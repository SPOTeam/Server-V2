package kr.spot.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import kr.spot.domain.Study;
import kr.spot.infrastructure.jpa.StudyRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

public class FakeStudyRepository implements StudyRepository {

  private final Map<Long, Study> storage = new HashMap<>();

  @Override
  public boolean existsById(Long id) {
    return storage.containsKey(id);
  }

  @Override
  public <S extends Study> S save(S entity) {
    storage.put(entity.getId(), entity);
    return entity;
  }

  @Override
  public Optional<Study> findById(Long id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public List<Study> findAll() {
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
  public void delete(Study entity) {
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
  public <S extends Study> S saveAndFlush(S entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study> List<S> saveAllAndFlush(Iterable<S> entities) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAllInBatch(Iterable<Study> entities) {
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
  public Study getOne(Long aLong) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Study getById(Long aLong) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Study getReferenceById(Long aLong) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study> Optional<S> findOne(Example<S> example) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study> List<S> findAll(Example<S> example) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study> List<S> findAll(Example<S> example, Sort sort) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study> Page<S> findAll(Example<S> example, Pageable pageable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study> long count(Example<S> example) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study> boolean exists(Example<S> example) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study, R> R findBy(Example<S> example,
      Function<FetchableFluentQuery<S>, R> queryFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends Study> List<S> saveAll(Iterable<S> entities) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Study> findAllById(Iterable<Long> longs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Study> findAll(Sort sort) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Page<Study> findAll(Pageable pageable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAllById(Iterable<? extends Long> longs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(Iterable<? extends Study> entities) {
    throw new UnsupportedOperationException();
  }
}
