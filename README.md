# Train Reservation System

A web-based Train Reservation System built using **Java 17 and Spring Boot**. The application allows users to search trains, calculate fares, reserve tickets, manage passenger details, view reservations, and cancel bookings. It also provides a separate Admin module for managing trains, routes, users, and reservations.

> Developed as part of the Java Development internship program at **Oasis Infobyte**.

---

## 🚀 Quick Access

### 🌐 Live Website

**https://train-reservation-system-ht5t.onrender.com/**

Open the link above to access the deployed Train Reservation System.

### 💻 GitHub Repository

**https://github.com/Rishav-Kundu/Train-Reservation-System**

### 🖥️ Run Locally

Clone the repository, configure a local MySQL database, and follow the [Running the Project Locally](#running-the-project-locally) section below.

---

## 📌 Features

### User Module

- User registration and login
- Secure authentication using Spring Security
- Role-based authorization
- Search trains between stations
- Case-insensitive station search
- View available trains
- Dynamic route-based fare calculation
- Book train tickets
- Enter details for multiple passengers
- Automatic PNR generation
- View personal reservations
- Cancel reservations
- Update profile information
- Change password

### Admin Module

- Admin authentication
- Role-based Admin access
- Admin dashboard
- View train and reservation statistics
- Add new trains
- Edit existing trains
- Delete trains
- Manage train routes
- Add and manage route stations
- View registered users
- View all reservations

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Application development |
| Spring Boot 3.5.x | Backend framework |
| Spring MVC | Web request handling |
| Spring Data JPA | Database access |
| Hibernate | Object-relational mapping |
| Spring Security | Authentication and authorization |
| BCrypt | Password hashing |
| Thymeleaf | Server-side HTML rendering |
| Bootstrap 5.3 | Responsive user interface |
| MySQL | Local development database |
| TiDB Cloud | Production MySQL-compatible database |
| Maven | Build and dependency management |
| Docker | Application containerization |
| Git & GitHub | Version control |
| Render | Cloud deployment |

---

## 🏗️ Application Architecture

The application follows a layered **Spring Boot MVC architecture**.

```text
                         Browser
                            │
                            ▼
                  Thymeleaf / Bootstrap
                            │
                            ▼
                       Controllers
                            │
                            ▼
                         Services
                            │
                            ▼
                   JPA Repositories
                            │
                            ▼
                     JPA / Hibernate
                            │
                            ▼
                 MySQL-compatible DB

Oasis Infobyte Java Development Internship
