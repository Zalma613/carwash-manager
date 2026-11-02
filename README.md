# CarWash Booking Manager

Egyszerű, konzolos autómosó időpont- és foglaláskezelő Java alkalmazás objektumorientált elvek, Strategy tervezési minta, valamint fájlalapú perzisztencia demonstrálására.

---

## 1. Projekt összefoglaló és problémafelvetés (Project Summary)

* **Célcsoport:** Kisebb és közepes autómosók adminisztrátorai, pénztárosai és műszakvezetői.
* **Felhasználói cél:** Új időpontok gyors felvétele, járművek és szolgáltatáscsomagok társítása, céges flottakedvezmények automatikus kalkulációja, foglalások listázása/rendezése, valamint az adatok tartós mentése.
* **Megoldott probléma:** Kiváltja a nehezen áttekinthető, kockázatos papíralapú vagy egyszerű táblázatos feljegyzéseket. Meggátolja a dupla foglalásokat (`DuplicateBookingException`), ellenőrzi a bemeneti adatokat (`ValidationException`), és biztosítja az állapot megőrzését (`try-with-resources` szöveges perzisztencia).

---

## 2. Előfeltételek (Prerequisites)

* **Java Development Kit (JDK):** Java 17 vagy újabb
* **Build eszköz:** Apache Maven 3.8+
* **Git:** Verziókezeléshez és a kód klónozásához

---

## 3. Fordítási, futtatási és tesztelési parancsok (Build & Run Instructions)

### 3.1 Tiszta klónozás és könyvtárba lépés
```bash
git clone git@github.com:Zalma613/carwash-manager.git
cd carwash-manager
```

### 3.2 Fordítás és egységtesztek futtatása
```bash
mvn clean test
```

### 3.3 Alkalmazás futtatása konzolról
```bash
mvn compile exec:java
```
*(vagy futtatható közvetlenül bármely elterjedt IDE-ből a `carwash.ui.Main` osztály indításával)*

---

## 4. Minta használat (Sample Usage)

```text
Üdvözöljük a CarWash Foglalási Rendszerben!

--- MENÜ ---
1. Új foglalás rögzítése
2. Foglalások listázása (Időpont szerint)
3. Foglalások listázása (Ár szerint növekvő)
4. Foglalások mentése fájlba
5. Foglalások betöltése fájlból
6. Kilépés
Válasszon menüpontot: 1

Időpont (pl. 2026-11-20 14:00): 2026-11-20 10:00
Ügyfél neve: Kovács János
Rendszám (pl. ABC-123): ABC-123
Mosási típus:
  1 - Alap mosás (3000 Ft)
  2 - Prémium mosás (6000 Ft)
Választás [1]: 2
Céges flotta partner? (i/n) [n]: i

✓ Sikeres foglalás!
Csomag: Prémium mosás | Kedvezmény: Flotta (-20%) | Fizetendő: 4800 Ft
```

---

## 5. Implementált követelmények (Implemented Requirements)

| Követelmény | Megvalósítás a projektben |
| :--- | :--- |
| **Egységbezárás (Encapsulation)** | Privát mezők (`private final`), érvényes állapotot garantáló konstruktorok és kontrollált getter metódusok a modell osztályokban (`Booking`, `Customer`, `WashType`). |
| **Absztrakció (Abstraction)** | A `WashType` absztrakt osztály és a `DiscountStrategy` interfész világos szerződést határoz meg a csomagok és kedvezmények számára. |
| **Öröklődés és polimorfizmus** | A `BasicWash` és `PremiumWash` osztályok felüldefiniálják a `WashType` metódusait; a `NoDiscount` és `FleetDiscount` a `DiscountStrategy`-t valósítja meg futásidőben felcserélhető módon. |
| **Objektum kapcsolatok** | Asszociáció és kompozíció: a `Booking` tartalmaz egy `Customer`-t és hivatkozik egy `WashType`-ra (1:1); a `BookingService` kezeli a `Booking` listát (1:0..*). |
| **Kollekciók és rendezés** | Generikus `List<Booking>` gyűjtemény. Kétféle rendezés: időpont szerint `Comparable<Booking>` interfésszel, ár szerint `Comparator`/lambda kifejezéssel. |
| **Kivételkezelés (Exceptions)** | Saját domain kivétel: `DuplicateBookingException` (ütköző időpontok kiszűrésére), valamint `ValidationException` (üres mezők elutasítására). |
| **Perzisztencia (Persistence)** | `BookingRepository` osztály szöveges fájlba (`bookings.txt`) ment és onnan tölt be, robusztus `try-with-resources` erőforrás-kezeléssel. |
| **Csomagszerkezet** | Felelősségek szétválasztása: `carwash.model`, `carwash.service`, `carwash.persistence`, `carwash.exception`, `carwash.ui`. |
| **Automatizált tesztek** | 7 JUnit 5 egységteszt (`src/test/java/carwash/BookingServiceTest.java`), amelyek 100%-ban lefedik az üzleti logikát és a perzisztenciát. |

---

## 6. Kiválasztott tervezési minta (Design Pattern)

* **Választott minta:** **Strategy (Stratégia) minta**
* **Résztvevő komponensek:**
  - `DiscountStrategy` (Stratégia interfész)
  - `NoDiscount` és `FleetDiscount` (Konkrét stratégia implementációk)
  - `BookingService` (Kliens kontextus, amely futásidőben alkalmazza a választott stratégiát)
* **Indoklás és előny:** A vállalkozás árazási és kedvezmény-szabályai függetleníthetők a foglalás rögzítésének menetétől. Bármikor hozzáadható új kedvezmény típus (pl. törzsvásárlói vagy hétvégi akció) anélkül, hogy a meglévő osztályokat módosítani kellene (Open-Closed Principle).
