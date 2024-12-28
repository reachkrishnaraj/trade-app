package com.kraj.tradeapp.core.model.persistance.dao;

import com.kraj.tradeapp.core.model.persistance.SignalCategoryRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class SignalCategoryRecordDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get the latest record for a given category
     * @param category the category to search for
     * @return Optional containing the latest record if found
     */
    public Optional<SignalCategoryRecord> getLatestByCategory(String category) {
        try {
            TypedQuery<SignalCategoryRecord> query = entityManager.createQuery(
                "SELECT s FROM SignalCategoryRecord s " + "WHERE s.category = :category " + "ORDER BY s.receivedDateTime DESC",
                SignalCategoryRecord.class
            );
            query.setParameter("category", category);
            query.setMaxResults(1);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Create a new signal category record
     * @param record the record to create
     * @return the created record with generated ID
     */
    public SignalCategoryRecord create(SignalCategoryRecord record) {
        entityManager.persist(record);
        return record;
    }

    /**
     * Read a signal category record by ID
     * @param id the ID of the record to read
     * @return Optional containing the record if found
     */
    public Optional<SignalCategoryRecord> read(String id) {
        SignalCategoryRecord record = entityManager.find(SignalCategoryRecord.class, id);
        return Optional.ofNullable(record);
    }

    /**
     * Update an existing signal category record
     * @param record the record to update
     * @return the updated record
     */
    public SignalCategoryRecord update(SignalCategoryRecord record) {
        return entityManager.merge(record);
    }

    /**
     * Delete a signal category record
     * @param record the record to delete
     */
    public void delete(SignalCategoryRecord record) {
        entityManager.remove(entityManager.contains(record) ? record : entityManager.merge(record));
    }

    /**
     * Delete a signal category record by ID
     * @param id the ID of the record to delete
     */
    public void deleteById(String id) {
        read(id).ifPresent(this::delete);
    }

    /**
     * Get all records for a given category
     * @param category the category to search for
     * @return List of all records for the category
     */
    public List<SignalCategoryRecord> getAllByCategory(String category) {
        TypedQuery<SignalCategoryRecord> query = entityManager.createQuery(
            "SELECT s FROM SignalCategoryRecord s WHERE s.category = :category",
            SignalCategoryRecord.class
        );
        query.setParameter("category", category);
        return query.getResultList();
    }
}
