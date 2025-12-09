package kr.spot.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.associations.StudyMemberRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

public class FakeStudyMemberRepository implements StudyMemberRepository {

  private final Map<Long, StudyMember> storage = new HashMap<>();

  @Override
  public StudyMember getById(Long id) {
    return findById(id)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
  }

  @Override
  public StudyMember getByStudyIdAndMemberIdAndStudyMemberStatus(Long studyId, Long memberId,
      StudyMemberStatus studyMemberStatus) {
    return findByStudyIdAndMemberIdAndStudyMemberStatus(studyId, memberId, studyMemberStatus)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
  }

  @Override
  public boolean existsByStudyIdAndMemberIdAndStudyMemberStatus(Long studyId, Long memberId,
      StudyMemberStatus studyMemberStatus) {
    return storage.values().stream()
        .anyMatch(sm -> sm.getStudyId().equals(studyId)
            && sm.getMemberId().equals(memberId)
            && sm.getStudyMemberStatus() == studyMemberStatus);
  }

  @Override
  public Optional<StudyMember> findByStudyIdAndMemberIdAndStudyMemberStatus(Long studyId,
      Long memberId, StudyMemberStatus studyMemberStatus) {
    return storage.values().stream()
        .filter(sm -> sm.getStudyId().equals(studyId)
            && sm.getMemberId().equals(memberId)
            && sm.getStudyMemberStatus() == studyMemberStatus)
        .findFirst();
  }

  @Override
  public <S extends StudyMember> S save(S entity) {
    storage.put(entity.getId(), entity);
    return entity;
  }

  @Override
  public Optional<StudyMember> findById(Long id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public boolean existsById(Long id) {
    return storage.containsKey(id);
  }

  @Override
  public List<StudyMember> findAll() {
    return new ArrayList<>(storage.values());
  }

  @Override
  public long count() {
    return storage.size();
  }

  @Override
  public void deleteById(Long id) {
    storage.remove(id);
  }

  @Override
  public void delete(StudyMember entity) {
    storage.remove(entity.getId());
  }

  @Override
  public void deleteAll() {
    storage.clear();
  }

  // Not implemented methods (throw UnsupportedOperationException)

  @Override
  public void flush() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> S saveAndFlush(S entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> List<S> saveAllAndFlush(Iterable<S> entities) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAllInBatch(Iterable<StudyMember> entities) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAllByIdInBatch(Iterable<Long> longs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAllInBatch() {
    throw new UnsupportedOperationException();
  }

  @Override
  public StudyMember getOne(Long aLong) {
    throw new UnsupportedOperationException();
  }

  @Override
  public StudyMember getReferenceById(Long aLong) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> Optional<S> findOne(Example<S> example) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> List<S> findAll(Example<S> example) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> List<S> findAll(Example<S> example, Sort sort) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> Page<S> findAll(Example<S> example, Pageable pageable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> long count(Example<S> example) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> boolean exists(Example<S> example) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember, R> R findBy(Example<S> example,
      Function<FetchableFluentQuery<S>, R> queryFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends StudyMember> List<S> saveAll(Iterable<S> entities) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<StudyMember> findAllById(Iterable<Long> longs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<StudyMember> findAll(Sort sort) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Page<StudyMember> findAll(Pageable pageable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAllById(Iterable<? extends Long> longs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(Iterable<? extends StudyMember> entities) {
    throw new UnsupportedOperationException();
  }
}
