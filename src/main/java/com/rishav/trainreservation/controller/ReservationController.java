package com.rishav.trainreservation.controller;

import com.rishav.trainreservation.entity.PassengerDetailsForm;
import com.rishav.trainreservation.entity.PassengerForm;
import com.rishav.trainreservation.entity.Reservation;
import com.rishav.trainreservation.entity.ReservationForm;
import com.rishav.trainreservation.entity.Train;
import com.rishav.trainreservation.entity.User;
import com.rishav.trainreservation.repository.ReservationRepository;
import com.rishav.trainreservation.repository.TrainRepository;
import com.rishav.trainreservation.service.ReservationService;
import com.rishav.trainreservation.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ReservationController {

    private final TrainRepository trainRepository;
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final ReservationService reservationService;

    public ReservationController(
            TrainRepository trainRepository,
            ReservationRepository reservationRepository,
            UserService userService,
            ReservationService reservationService) {

        this.trainRepository = trainRepository;
        this.reservationRepository = reservationRepository;
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping("/reserve/{trainId}")
    public String reservePage(
            @PathVariable Long trainId,
            @RequestParam String from,
            @RequestParam String to,
            Model model) {

        Train train =
                trainRepository.findById(trainId)
                        .orElseThrow();

        double fare =
                reservationService.calculateFare(
                        train,
                        from,
                        to);

        ReservationForm form =
                new ReservationForm();

        form.setFromStation(from);
        form.setToStation(to);

        model.addAttribute(
                "train",
                train);

        model.addAttribute(
                "fare",
                fare);

        model.addAttribute(
                "reservationForm",
                form);

        return "reserve-ticket";
    }

    @PostMapping("/reserve/{trainId}")
    public String preparePassengerDetails(
            @PathVariable Long trainId,
            @ModelAttribute ReservationForm reservationForm,
            Model model) {

        Train train =
                trainRepository.findById(trainId)
                        .orElseThrow();

        double fare;

        try {

            fare =
                    reservationService.calculateFare(
                            train,
                            reservationForm.getFromStation(),
                            reservationForm.getToStation());

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage());

            model.addAttribute(
                    "train",
                    train);

            model.addAttribute(
                    "reservationForm",
                    reservationForm);

            return "reserve-ticket";
        }

        if (reservationForm.getJourneyDate() == null) {

            model.addAttribute(
                    "error",
                    "Journey date is required.");

            model.addAttribute(
                    "train",
                    train);

            model.addAttribute(
                    "fare",
                    fare);

            model.addAttribute(
                    "reservationForm",
                    reservationForm);

            return "reserve-ticket";
        }

        if (reservationForm.getPassengerCount() == null ||
                reservationForm.getPassengerCount() <= 0 ||
                reservationForm.getPassengerCount() > 6) {

            model.addAttribute(
                    "error",
                    "Passenger count must be between 1 and 6.");

            model.addAttribute(
                    "train",
                    train);

            model.addAttribute(
                    "fare",
                    fare);

            model.addAttribute(
                    "reservationForm",
                    reservationForm);

            return "reserve-ticket";
        }

        if (reservationForm.getPassengerCount() >
                train.getAvailableSeats()) {

            model.addAttribute(
                    "error",
                    "Not enough seats available.");

            model.addAttribute(
                    "train",
                    train);

            model.addAttribute(
                    "fare",
                    fare);

            model.addAttribute(
                    "reservationForm",
                    reservationForm);

            return "reserve-ticket";
        }

        PassengerDetailsForm passengerDetailsForm =
                new PassengerDetailsForm();

        passengerDetailsForm.setTrainId(
                trainId);

        passengerDetailsForm.setFromStation(
                reservationForm.getFromStation());

        passengerDetailsForm.setToStation(
                reservationForm.getToStation());

        passengerDetailsForm.setJourneyDate(
                reservationForm.getJourneyDate());

        passengerDetailsForm.setPassengerCount(
                reservationForm.getPassengerCount());

        for (int i = 0;
             i < reservationForm.getPassengerCount();
             i++) {

            passengerDetailsForm
                    .getPassengers()
                    .add(new PassengerForm());
        }

        model.addAttribute(
                "fare",
                fare);

        model.addAttribute(
                "totalFare",
                fare * reservationForm.getPassengerCount());

        model.addAttribute(
                "passengerDetailsForm",
                passengerDetailsForm);

        model.addAttribute(
                "train",
                train);

        return "enter-passengers";
    }

    @PostMapping("/confirm-booking")
    public String confirmBooking(
            @ModelAttribute PassengerDetailsForm form,
            Model model) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                userService.findByEmail(
                        authentication.getName());

        try {

            reservationService.createReservation(
                    form,
                    user);

            return "redirect:/my-reservations";

        } catch (IllegalArgumentException |
                 SecurityException e) {

            Train train =
                    trainRepository.findById(
                            form.getTrainId())
                            .orElseThrow();

            double fare =
                    reservationService.calculateFare(
                            train,
                            form.getFromStation(),
                            form.getToStation());

            model.addAttribute(
                    "train",
                    train);

            model.addAttribute(
                    "fare",
                    fare);

            model.addAttribute(
                    "totalFare",
                    fare * form.getPassengerCount());

            model.addAttribute(
                    "passengerDetailsForm",
                    form);

            model.addAttribute(
                    "error",
                    e.getMessage());

            return "enter-passengers";
        }
    }

    @GetMapping("/my-reservations")
    public String myReservations(
            Model model) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                userService.findByEmail(
                        authentication.getName());

        model.addAttribute(
                "reservations",
                reservationRepository.findByUser(user));

        return "my-reservations";
    }

    @GetMapping("/cancel-ticket")
    public String cancelTicketPage() {

        return "cancel-ticket";
    }

    @PostMapping("/cancel-ticket/search")
    public String searchPnr(
            @RequestParam String pnr,
            Model model) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                userService.findByEmail(
                        authentication.getName());

        Reservation reservation =
                reservationRepository.findByPnr(pnr);

        if (reservation == null) {

            model.addAttribute(
                    "error",
                    "No reservation found with the entered PNR.");

            return "cancel-ticket";
        }

        if (!reservation.getUser()
                .getId()
                .equals(user.getId())) {

            model.addAttribute(
                    "error",
                    "You are not authorized to access this reservation.");

            return "cancel-ticket";
        }

        model.addAttribute(
                "reservation",
                reservation);

        return "reservation-details";
    }

    @PostMapping("/cancel-ticket/confirm")
    public String confirmCancellation(
            @RequestParam Long reservationId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                userService.findByEmail(
                        authentication.getName());

        try {

            reservationService.cancelReservation(
                    reservationId,
                    user);

        } catch (SecurityException |
                 IllegalArgumentException e) {

            return "redirect:/cancel-ticket";
        }

        return "redirect:/my-reservations";
    }
}

