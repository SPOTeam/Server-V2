package kr.spot.infrastructure.seed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "region.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
class RegionSeeder implements ApplicationRunner {

  private final DataSource dataSource;

  // classpath: modules/region/src/main/resources/data/region_data.tsv
  @Value("classpath:data/region_data.tsv")
  private Resource tsv;

  // 기본은 LF, 윈도우면 "\r\n"로 application.yml에서 덮어씀
  @Value("${region.seed.lines-terminated:\\n}")
  private String linesTerminated;

  // \n, \r\n 같은 제어문자를 SQL 문자열 리터럴로 escape
  private static String escapeForSql(String s) {
    return s.replace("\\", "\\\\")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
        .replace("'", "\\'");
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (!tsv.exists()) {
      throw new IllegalStateException("Region TSV not found on classpath: " + tsv);
    }

    // classpath → 임시 파일 (LOAD DATA는 파일 경로 필요)
    Path tmp = Files.createTempFile("region-", ".tsv");
    try (InputStream in = tsv.getInputStream()) {
      Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    String infile = tmp.toAbsolutePath().toString().replace("\\", "\\\\");
    log.info("[RegionSeeder] Seeding region from: {}", infile);

    try (var conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
      conn.setAutoCommit(false);

      try {
        st.execute("SET SESSION local_infile = 1");
      } catch (Exception ignore) {
      }

      // 1) 스테이징 테이블
      st.execute("""
              CREATE TEMPORARY TABLE tmp_region (
                code VARCHAR(20) NOT NULL,
                province VARCHAR(50) NOT NULL,
                district VARCHAR(50) NOT NULL,
                neighborhood VARCHAR(50) NOT NULL
              )
          """);

      boolean loaded = false;

      // 2) 1차: LOAD DATA 시도
      try {
        String load = ("""
                LOAD DATA LOCAL INFILE '%s'
                INTO TABLE tmp_region
                CHARACTER SET utf8mb4
                FIELDS TERMINATED BY '\\t'
                LINES TERMINATED BY '%s'
                IGNORE 1 LINES
                (code, province, district, neighborhood)
            """).formatted(infile, escapeForSql(linesTerminated));
        st.execute(load);
        loaded = true;
        log.info("[RegionSeeder] Loaded via LOAD DATA LOCAL INFILE");
      } catch (Exception ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        // local_infile 비활성/권한 문제면 폴백
        if (msg.contains("loading local data is disabled") || msg.contains("local infile")) {
          log.warn("[RegionSeeder] LOAD DATA disabled → fallback to JDBC batch: {}",
              ex.getMessage());
          try (var br = new BufferedReader(new InputStreamReader(tsv.getInputStream()));
              var ps = conn.prepareStatement("""
                     INSERT INTO tmp_region(code, province, district, neighborhood)
                     VALUES (?, ?, ?, ?)
                  """)) {

            String line;
            boolean header = true;
            int batch = 0;
            while ((line = br.readLine()) != null) {
              if (header) {
                header = false;
                continue;
              } // 헤더 스킵
              if (line.isEmpty()) {
                continue;
              }
              // 탭 기준: code	province	district	neighborhood
              String[] c = line.split("\\t", -1);
              if (c.length < 4) {
                continue; // 방어
              }
              ps.setString(1, c[0]);
              ps.setString(2, c[1]);
              ps.setString(3, c[2]);
              ps.setString(4, c[3]);
              ps.addBatch();
              if (++batch % 1000 == 0) {
                ps.executeBatch();
              }
            }
            ps.executeBatch();
            loaded = true;
          }
        } else {
          // 다른 오류는 그대로 전파
          throw ex;
        }
      }

      if (!loaded) {
        conn.rollback();
        throw new IllegalStateException("Region seeding failed: no loader succeeded.");
      }

      // 3) 업서트 (PK/UNIQUE = code 필요)
      st.execute("""
              INSERT INTO region (code, province, district, neighborhood)
              SELECT code, province, district, neighborhood
              FROM tmp_region AS src
              ON DUPLICATE KEY UPDATE
                province = src.province,
                district = src.district,
                neighborhood = src.neighborhood
          """);

      conn.commit();
      log.info("[RegionSeeder] Region seed completed.");
    } finally {
      try {
        Files.deleteIfExists(tmp);
      } catch (Exception ignore) {
      }
    }
  }
}
