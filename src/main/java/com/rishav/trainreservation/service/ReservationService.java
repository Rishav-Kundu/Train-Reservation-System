package com.rishav.trainreservation.service;

import com.rishav.trainreservation.entity.Passenger;
import com.rishav.trainreservation.entity.PassengerDetailsForm;
import com.rishav.trainreservation.entity.PassengerForm;
import com.rishav.trainreservation.entity.Reservation;
import com.rishav.trainreservation.entity.Train;
import com.rishav.trainreservation.entity.TrainRoute;
import com.rishav.trainreservation.entity.User;
import com.rishav.trainreservation.repository.ReservationRepository;
import com.rishav.trainreservation.repository.TrainRepository;
import com.rishav.trainreservation.repository.TrainRouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final TrainRepository trainRepository;
    private final TrainRouteRepository trainRouteRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(
            TrainRepository trainRepository,
            TrainRouteRepository trainRouteRepository,
            ReservationRepository reservationRepository) {

        this.trainRepository = trainRepository;
        this.trainRouteRepository = trainRouteRepository;
        this.reservationRepository = reservationRepository;
    }

    public double calculateFare(
            Train train,
            String fromStation,
            String toStation) {

        if (fromStation == null ||
                toStation == null ||
                fromStation.isBlank() ||
                toStation.isBlank()) {

            throw new IllegalArgumentException(
                    "Source and destination are required.");
        }

        TrainRoute fromRoute =
                trainRouteRepository.findByTrainAndStationName(
                        train,
                        fromStation);

        TrainRoute toRoute =
                trainRouteRepository.findByTrainAndStationName(
                        train,
                        toStation);

        if (fromRoute == null ||
                toRoute == null) {

            throw new IllegalArgumentException(
                    "Invalid source or destination station.");
        }

        if (fromRoute.getStationOrder() >=
                toRoute.getStationOrder()) {

            throw new IllegalArgumentException(
                    "Destination must come after source.");
        }

        int stationsTravelled =
                toRoute.getStationOrder()
                        - fromRoute.getStationOrder();

        return train.getBaseFare()
                + (stationsTravelled
                * train.getFarePerStation());
    }

    @Transactional
    public Reservation createReservation(
            PassengerDetailsForm form,
            User user) {

        if (form.getTrainId() == null) {

            throw new IllegalArgumentException(
                    "Train is required.");
        }

        if (form.getJourneyDate() == null) {

            throw new IllegalArgumentException(
                    "Journey date is required.");
        }

        if (form.getJourneyDate()
                .isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Journey date cannot be in the past.");
        }

        if (form.getPassengerCount() == null ||
                form.getPassengerCount() <= 0 ||
                form.getPassengerCount() > 6) {

            throw new IllegalArgumentException(
                    "Passenger count must be between 1 and 6.");
        }

        List<PassengerForm> passengerForms =
                form.getPassengers();

        if (passengerForms == null ||
                passengerForms.size()
                        != form.getPassengerCount()) {

            throw new IllegalArgumentException(
                    "Passenger details do not match passenger count.");
        }

        /*
         * Retrieve the train inside the transaction.
         * This will be replaced with a locked query
         * in the next step to handle concurrent bookings.
         */
        Train train =
                trainRepository.findByIdForUpdate(
                        form.getTrainId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Train not found."));

        if (form.getPassengerCount() >
                train.getAvailableSeats()) {

            throw new IllegalArgumentException(
                    "Not enough seats available.");
        }

        /*
         * IMPORTANT:
         * Fare is calculated again on the server.
         * We do NOT use a fare supplied by the browser.
         */
        double fare =
                calculateFare(
                        train,
                        form.getFromStation(),
                        form.getToStation());

        Reservation reservation =
                new Reservation();

        reservation.setPnr(
                generatePnr());

        reservation.setJourneyDate(
                form.getJourneyDate());

        reservation.setSource(
                form.getFromStation());

        reservation.setDestination(
                form.getToStation());

        reservation.setPassengerCount(
                form.getPassengerCount());

        reservation.setTotalFare(
                fare * form.getPassengerCount());

        reservation.setStatus(
                "CONFIRMED");

        reservation.setCreatedAt(
                LocalDateTime.now());

        reservation.setUser(user);

        reservation.setTrain(train);

        List<Passenger> passengers =
                new ArrayList<>();

        for (PassengerForm passengerForm :
                passengerForms) {

            if (passengerForm.getName() == null ||
                    passengerForm.getName().isBlank()) {

                throw new IllegalArgumentException(
                        "Passenger name is required.");
            }

            if (passengerForm.getAge() == null ||
                    passengerForm.getAge() < 1 ||
                    passengerForm.getAge() > 120) {

                throw new IllegalArgumentException(
                        "Passenger age must be between 1 and 120.");
            }

            if (passengerForm.getGender() == null ||
                    passengerForm.getGender().isBlank()) {

                throw new IllegalArgumentException(
                        "Passenger gender is required.");
            }

            Passenger passenger =
                    new Passenger();

            passenger.setName(
                    passengerForm.getName().trim());

            passenger.setAge(
                    passengerForm.getAge());

            passenger.setGender(
                    passengerForm.getGender());

            passenger.setReservation(
                    reservation);

            passengers.add(passenger);
        }

        reservation.setPassengers(
                passengers);

        train.setAvailableSeats(
                train.getAvailableSeats()
                        - form.getPassengerCount());

        trainRepository.save(train);

        return reservationRepository.save(
                reservation);
    }

    @Transactional
    public void cancelReservation(
            Long reservationId,
            User user) {

        Reservation reservation =
                reservationRepository.findById(
                        reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found."));

        if (!reservation.getUser()
                .getId()
                .equals(user.getId())) {

            throw new SecurityException(
                    "You are not authorized to cancel this reservation.");
        }

        if ("CANCELLED".equals(
                reservation.getStatus())) {

            return;
        }

        Train train =
                trainRepository.findById(
                        reservation.getTrain().getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Train not found."));

        train.setAvailableSeats(
                train.getAvailableSeats()
                        + reservation.getPassengerCount());

        reservation.setStatus(
                "CANCELLED");

        trainRepository.save(train);

        reservationRepository.save(
                reservation);
    }

    private String generatePnr() {

        return "PNR"
                + System.currentTimeMillis();
    }
}
