package com.rishav.trainreservation.repository;

import com.rishav.trainreservation.entity.Train;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrainRepository
        extends JpaRepository<Train, Long> {

    Train findByTrainNo(String trainNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Train t WHERE t.id = :id")
    Optional<Train> findByIdForUpdate(@Param("id") Long id);
}