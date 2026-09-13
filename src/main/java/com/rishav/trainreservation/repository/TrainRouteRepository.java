
package com.rishav.trainreservation.repository;

import com.rishav.trainreservation.entity.Train;
import com.rishav.trainreservation.entity.TrainRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainRouteRepository
        extends JpaRepository<TrainRoute, Long> {

    List<TrainRoute> findByStationNameIgnoreCase(String stationName);

    List<TrainRoute> findByTrainOrderByStationOrderAsc(
            Train train);

    long countByTrain(Train train);

    TrainRoute findByTrainAndStationNameIgnoreCase(
            Train train,
            String stationName);
}

