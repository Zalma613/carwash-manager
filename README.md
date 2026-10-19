# CarWash Booking Manager

A console-based Java application designed to manage vehicle wash appointments, pricing tiers, and fleet discounts with local file persistence.

---

## 1. Problem Statement
* **Target Users:** Small-to-medium car wash operators and receptionists who handle both individual retail customers and corporate fleet partners.
* **User Goal:** Quickly record, list, sort, and persist vehicle wash bookings without double-booking time slots or manually calculating complex partner discounts.
* **Problem Solved:** Replaces error-prone paper logs and spreadsheets with an object-oriented system that enforces domain rules (such as preventing duplicate time slots), automates service and discount pricing at runtime, and reliably saves/restores data between sessions.

---

## 2. Prerequisites
* **Java Development Kit (JDK):** Version 17 or higher
* **Build Tool:** Apache Maven 3.8+
* **Git:** For version control and clean checkout

---

## 3. Build, Run, and Test Instructions

Clone the repository and navigate into the project root directory:
```bash
git clone git@github.com:<YOUR_GITHUB_USERNAME>/carwash-manager.git
cd carwash-manager
