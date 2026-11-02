>># User Stories

### US1: Új időpont foglalása
* **Szereplő:** Autómosó adminisztrátor / recepciós
* **Cél:** Új foglalást tudjon rögzíteni az ügyfél nevével, rendszámával, mosási típusával, kedvezmény kategóriájával és a kívánt időponttal.
* **Elfogadási feltétel:** A rendszer ne engedje ugyanarra az időpontra kétszer foglalni (dobjon `DuplicateBookingException`-t), valamint a mezők érvényesítésre kerüljenek (üres mezők esetén `ValidationException`).

### US2: Kedvezmények érvényesítése
* **Szereplő:** Pénztáros / recepciós
* **Cél:** A céges flotta partnerek számára automatikusan vonjon le 20% kedvezményt a szolgáltatás árából (Strategy minta).
* **Elfogadási feltétel:** Flotta ügyfél választásakor az alapár csökkentett összege jelenjen meg a fizetendő összegként.

### US3: Foglalások áttekintése és rendezése
* **Szereplő:** Műszakvezető / mosó személyzet
* **Cél:** Kilistázni az összes foglalást időrendben (hogy tudják a napi beosztást) vagy ár szerint (hogy lássák a legnagyobb értékű munkákat).
* **Elfogadási feltétel:** A lista mutassa az ügyfél nevét, rendszámát, időpontot, szolgáltatást és a pontos árat. Időpont szerint `Comparable<Booking>`, ár szerint lambda-alapú rendezéssel történjen a kiírás.

### US4: Adatok mentése és betöltése
* **Szereplő:** Rendszeradminisztrátor
* **Cél:** A foglalási adatok perzisztálása helyi szöveges fájlba (`bookings.txt`), illetve korábbi állapot betöltése indításkor vagy kérésre.
* **Elfogadási feltétel:** Hiba esetén a program kezelje az `IOException`-t, és biztosítsa a fájlerőforrások lezárását `try-with-resources` szerkezettel.
