# Hogyan működik a kód? – Teljes, részletes útmutató

Ez a dokumentum lépésről lépésre, emberi nyelven elmagyarázza, hogy pontosan mit csinál az autómosó foglalási rendszer, melyik osztály miért felelős, hogyan fut a kód, és mik a válaszok az oktató esetleges kérdéseire a védésen.

---

## 1. Rövid összefoglaló (Mit csinál a program?)

A program egy **autómosó foglalási rendszere**, amely a terminálban fut. 
A felhasználó (recepciós / pénztáros) képes:
1. **Új időpontot felvenni:** Megadja az ügyfél nevét, a rendszámot, a választott mosási típust, kiválasztja, hogy jogosult-e flottakedvezményre, és megadja a kívánt időpontot.
2. **Kiszámolni az árat:** A rendszer a választott mosás alapárából és az érvényes kedvezményből automatikusan kiszámolja a végső árat.
3. **Ütközést és hibás adatot megelőzni:** Nem enged két autót ugyanarra az időpontra beírni, és nem enged üresen hagyni kötelező adatokat.
4. **Listázni és rendezni:** A foglalásokat ki tudja írni időrendben vagy az ár növekvő sorrendjében.
5. **Fájlba menteni és betölteni:** A foglalásokat lementi a merevlemezre egy szöveges fájlba (`bookings.txt`), így a program újraindítása után sem vesznek el az adatok.

---

## 2. A kód felépítése – Fájlról fájlra elmagyarázva

A projekt 5 külön mappára (csomagra) van bontva a tiszta felelősségmegosztás miatt.

```
src/main/java/carwash/
├── exception/          --> Hibakezelés (saját kivételek)
├── model/              --> Adatok és üzleti szabályok (Ügyfél, Mosás, Kedvezmény, Foglalás)
├── persistence/        --> Fájlba mentés és betöltés
├── service/            --> A háttérben futó üzleti logika és ellenőrzések
└── ui/                 --> A terminálos menü és felhasználói felület
```

---

### A) `carwash.model` csomag (Az adatok modellje)

#### 1. `Customer.java` (Ügyfél)
* **Mit csinál?** Egy ügyfél adatait tárolja (jelenleg a nevét).
* **Miért van külön?** Mert az OOP világában egy ügyfél nem csak egy puszta szöveg, hanem egy önálló entitás.
* **Hogyan védi az adatot?** A konstruktorban rögtön ellenőrzi: ha a név `null` vagy csak szóközökből áll, nem engedi létrehozni az objektumot, hanem hibát (`ValidationException`) dob.

#### 2. `WashType.java` (Mosási típus - Absztrakt ősosztály)
* **Mit csinál?** Ez egy közös „sablon” (szerződés) a mosási csomagoknak.
* **Mit tartalmaz?** Két absztrakt metódust:
  * `getName()`: visszaadja a mosás nevét (pl. "Alap mosás").
  * `getBasePrice()`: visszaadja az alapárat (pl. 3000 Ft).
* **Miért absztrakt?** Mert általános „mosás” nem létezik önmagában, csak konkrét változatok (Alap, Prémium).

#### 3. `BasicWash.java` és `PremiumWash.java` (Konkrét mosások)
* **Mit csinálnak?** Mindketten öröklik a `WashType`-ot (`extends WashType`).
* **Különbségük:**
  * `BasicWash`: Név = "Alap mosás", Alapár = 3000 Ft.
  * `PremiumWash`: Név = "Prémium mosás", Alapár = 6000 Ft.
* **Mi ez a tananyagban?** **Öröklődés és polimorfizmus.** Bárhol használhatjuk a szülő `WashType` típust, és a program futás közben dönti el, melyik árát kéri le.

#### 4. `DiscountStrategy.java` (Kedvezmény interfész - Strategy minta)
* **Mit csinál?** Meghatározza a kedvezményszámítás szabályát: van egy `calculate(double basePrice)` metódusa.
* **Miért interface?** Hogy a program tetszőleges számú kedvezményformát tudjon kezelni anélkül, hogy a fő kódot módosítani kellene.

#### 5. `NoDiscount.java` és `FleetDiscount.java` (A stratégiák)
* Mindketten megvalósítják a `DiscountStrategy` interfészt (`implements DiscountStrategy`):
  * `NoDiscount`: Nem változtat semmit, visszaadja az eredeti árat (`return basePrice;`).
  * `FleetDiscount`: Céges flottáknak 20% kedvezményt ad (`return basePrice * 0.80;`).

#### 6. `Booking.java` (Egy darab foglalás)
* **Mit tartalmaz?** 
  * `Customer customer` (Ki foglalta?)
  * `String licensePlate` (Mi az autó rendszáma?)
  * `WashType washType` (Milyen csomagot kért?)
  * `double price` (Mennyi a fizetendő végösszeg a kedvezmény után?)
  * `String timeSlot` (Mikorra szól a foglalás? pl. "2026-11-20 10:00")
* **Hogyan garantálja az érvényességet?** A konstruktorban minden egyes mezőt leellenőriz (nem üres-e a rendszám, pozitív-e az ár).
* **Miért valósítja meg a `Comparable<Booking>` interfészt?**
  Hogy a foglalásoknak legyen egy **természetes rendezettsége**. A `compareTo` metódus az időpont (`timeSlot`) alapján hasonlítja össze a foglalásokat, így egyetlen `Collections.sort(lista)` hívással időrendbe tehetők.

---

### B) `carwash.exception` csomag (Hibakezelés)

1. **`DuplicateBookingException.java`:**
   * Saját ellenőrzött kivétel (`extends Exception`).
   * Akkor dobja a rendszer, ha egy olyan időpontra próbálnak új időpontot rögzíteni, ami már foglalt.
2. **`ValidationException.java`:**
   * Akkor dobódik, ha a felhasználó üresen hagyja a nevet, rendszámot, időpontot, vagy értelmetlen adatot ad meg.

---

### C) `carwash.persistence` csomag (Adattárolás)

#### `BookingRepository.java`
* **Mit csinál?** Kapcsolatot tart a merevlemezen lévő `bookings.txt` fájllal.
* **Mentés (`save`):**
  * Megnyitja a fájlt írásra `try-with-resources` (`BufferedWriter`) szerkezettel (ez garantálja, hogy a fájl hiba esetén is lezárul, nincs memóriaszivárgás).
  * Pontosvesszővel elválasztva kiírja a sorokat:
    `2026-11-20 10:00;Kovács János;ABC-123;Alap mosás;3000`
* **Betöltés (`load`):**
  * Ha még nem létezik a fájl, üres listát ad vissza (nem omlik össze).
  * Ha létezik, soronként beolvassa `BufferedReader`-rel, feldarabolja a pontosvesszők mentén, újraépíti a `Customer`, `WashType` és `Booking` objektumokat, majd visszaadja őket egy listában.

---

### D) `carwash.service` csomag (Üzleti logika)

#### `BookingService.java`
* **Mit csinál?** Ez a rendszer „agya”. Nem tud a terminálról, nem tud a konzolról, csak a tiszta logikát intézi:
  1. `addBooking(...)`:
     * Elsőként végignézi a meglévő foglalásokat: ha az időpont már foglalt, eldobja a `DuplicateBookingException`-t!
     * Létrehozza a `Customer`-t (ez ellenőrzi a nevet).
     * Meghívja a `discountStrategy.calculate(...)` metódust az ár kiszámításához.
     * Létrehozza a `Booking`-ot és elteszi a belső `List<Booking>` listába.
  2. `getBookingsSortedByTime()`:
     * Időrendbe rendezi a foglalásokat a `Comparable` segítségével.
  3. `getBookingsSortedByPrice()`:
     * Ár szerint növekvő sorrendbe rendezi a foglalásokat modern Java lambda-kifejezéssel: `Comparator.comparingDouble(Booking::getPrice)`.
  4. `saveToFile()` és `loadFromFile()`:
     * Szól a `BookingRepository`-nak, hogy végezze el a lemezműveletet.

---

### E) `carwash.ui` csomag (Felhasználói felület)

#### `Main.java`
* **Mit csinál?** Ez tartalmazza a `public static void main` indítópontot.
* Kiírja a menüt egy `while(running)` ciklusban:
  1. Új foglalás rögzítése
  2. Foglalások listázása (Időpont szerint)
  3. Foglalások listázása (Ár szerint növekvő)
  4. Foglalások mentése fájlba
  5. Foglalások betöltése fájlból
  6. Kilépés
* Beolvassa a felhasználó gépelését `Scanner`-rel, átadja a `BookingService`-nek, és ha hiba történik (pl. duplikáció vagy üres adat), szépen kiírja magyarul a hibaüzenetet ahelyett, hogy a program piros betűkkel összeomlana.

---

### F) `src/test/java/carwash` (Egységtesztek)

#### `BookingServiceTest.java`
* **Mit csinál?** 7 automatikus JUnit 5 tesztet futtat le a háttérben:
  1. Normál ár kalkulációja (nincs kedvezmény).
  2. Flotta kedvezmény kalkulációja (-20%).
  3. Ütközésvizsgálat (dob-e hibát azonos időpontnál).
  4. Mező-validáció (üres név elutasítása).
  5. Időpont szerinti rendezés helyessége.
  6. Ár szerinti rendezés helyessége.
  7. Fájlba mentés és visszatöltés helyessége ideiglenes tesztfájllal.

---

## 3. Gyors puska a védéshez (Mit válaszolj az oktatónak?)

Ha a tanár megkérdezi:

1. **„Hol van az egységbezárás (Encapsulation)?”**
   * *Válasz:* A `Booking` és `Customer` osztályokban minden mező `private final`. Nem lehet kívülről közvetlenül módosítani őket, csak a konstruktoron keresztül hozhatók létre, ami azonnal le is ellenőrzi az adatok érvényességét. Olvasni pedig getterekkel lehet.

2. **„Hol valósul meg az öröklődés és polimorfizmus?”**
   * *Válasz:* A `WashType` absztrakt osztályból származik le a `BasicWash` és a `PremiumWash`. A `BookingService` és a `Booking` a közös ősosztályt (`WashType`) használja, így futásidőben dől el, hogy melyik konkrét csomagnév és alapár hívódik meg.

3. **„Milyen tervezési mintát használtál és miért?”**
   * *Válasz:* A **Strategy (Stratégia)** mintát a kedvezmények kezelésére. A `DiscountStrategy` interfészt valósítja meg a `NoDiscount` és a `FleetDiscount`. Az előnye az Open-Closed elv: ha a jövőben új akciót vezetünk be (pl. hétvégi kedvezmény), egyetlen új osztály megírásával megtehetjük, a meglévő foglalási kódot nem kell módosítani.

4. **„Milyen kollekciót és rendezést használsz?”**
   * *Válasz:* Generikus `ArrayList<Booking>` listát. Kétféleképpen tudunk rendezni: időpont szerint a `Comparable<Booking>` interfésszel (`Collections.sort`), illetve ár szerint növekvő sorrendben egyedi `Comparator` lambda-kifejezéssel (`Comparator.comparingDouble(Booking::getPrice)`).

5. **„Hogyan kezeled a hibákat és a perzisztenciát?”**
   * *Válasz:* Saját kivétellel (`DuplicateBookingException`), ha ütközik az időpont, illetve `ValidationException`-nel rossz bemenet esetén. A perzisztenciát a `BookingRepository` végzi szöveges fájlba (`bookings.txt`), és `try-with-resources` blokkot használunk, ami automatikusan és garantáltan lezárja a leírókat.
