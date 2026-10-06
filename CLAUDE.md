# JavaSplendor

เกม Splendor-like (เครื่องเดียว + bot คู่ต่อสู้) โปรเจกต์วิชา OOP ด้วย Java + libGDX (Gradle)
- `core/` = logic เกม + หน้าจอ (`io.github.some_example_name`)
- `lwjgl3/` = launcher desktop
- รัน: `./gradlew lwjgl3:run` / build: `./gradlew build`

## โครงสร้างปัจจุบัน (core)
- `Gem`: เหรียญ 6 สี (red, blue, green, white, black, gold) ใช้เป็น bank, เหรียญผู้เล่น, ราคาการ์ด
- `Card`: id, level, point, color (String), cardCost (Gem)
- `Noble`: id, point, requirement 5 สี
- `Deck`: กองการ์ด (addCard, drawCard, shuffle)
- `Board`: bank (7,7,7,7,7,5), Deck level1-3, การ์ดที่เปิดอยู่ level1-3, nobles
- `Player`: name, score, gems, cards, reservedCards, nobles
- `Game`: players[4] (fix), board, currentPlayer, nextTurn()
- `GameManager`: takeXGem() x6, buyCard(), endTurn()
- `Main`, `FirstScreen`: libGDX; FirstScreen ตอนนี้เป็นหน้าทดสอบ (SPACE = หยิบแดง, ENTER = จบตา)

## ปัญหา logic ที่ต้องแก้ (เรียงความสำคัญ)
1. `buyCard` ผิดกฎ: ไม่คิดส่วนลดจากการ์ดที่ซื้อแล้ว (bonus), ไม่ใช้ทองแทนสีที่ขาด, เหรียญที่จ่ายไม่คืน bank, ไม่เช็คว่าการ์ดอยู่บนกระดาน, ไม่เติมการ์ดใหม่จากกอง
2. หยิบเหรียญไม่มีกติกา: ต้องเป็น 3 สีต่างกัน หรือ 2 เหรียญสีเดียวกัน (ต้องเหลือในกอง >= 4); ถือได้ไม่เกิน 10 (เกินต้องคืน)
3. ไม่มี reserve ใน GameManager (จอง max 3 ใบ, ได้ทอง 1 ถ้ามี)
4. ขุนนางยังไม่ทำงาน: เช็คหลังซื้อการ์ด, ให้ +3 แต้ม
5. ไม่มี win condition: ถึง 15 แต้มแล้วเล่นจนครบรอบ, เสมอ = การ์ดน้อยกว่าชนะ
6. Setup ไม่ครบ: ไม่มีข้อมูลการ์ด 90 ใบ/ขุนนาง 10 ใบ, ไม่สับ/เปิด 4 ใบต่อ level, เหรียญและขุนนางไม่ปรับตามจำนวนผู้เล่น (2 คน = สีละ 4, 3 = 5, 4 = 7; ขุนนาง = ผู้เล่น+1), Game fix 4 คน
7. ยังไม่มี bot

## ปัญหาดีไซน์
- `Gem` ซ้ำซ้อน (18 เมธอด) และ `GameManager.takeXGem()` ซ้ำ 6 รอบ -> ใช้ `enum GemColor` + `EnumMap`
- `Card.color` เป็น String -> ใช้ `GemColor`
- ชื่อ `Game` ชนกับ `com.badlogic.gdx.Game` -> เปลี่ยนเป็น `GameState`
- `Board` เก็บ level 1/2/3 แยกตัวแปรซ้ำ -> ใช้ array/list ตาม index level
- `Player.score` เก็บแยก -> คำนวณจาก cards + nobles
- `buyCard` และ action อื่นควรคืน boolean/ผลลัพธ์ให้ UI/bot รู้
- `endTurn` ไม่เช็คว่าทำ action แล้ว

## โครงเป้าหมาย
```
GemColor (enum)       แทน String และตัวแปร 6 สี
Gem/Wallet            EnumMap
Card, Noble           ใช้ GemColor
Player                getBonus(color), score คำนวณ
Board                 setup ตามจำนวนผู้เล่น, เติมการ์ด
GameState             players, board, turn, สถานะจบเกม
GameManager (rules)   takeThree, takeTwo, buy, reserve, checkNobles, checkWin
Bot                   interface chooseAction(GameState) -> Action
```

## ลำดับงาน
1. refactor GemColor / Gem  2. buyCard ถูกกฎ (ส่วนลด + ทอง)  3. กติกาหยิบเหรียญ
4. โหลดการ์ด/ขุนนาง + setup  5. reserve / nobles / win  6. bot  7. UI จริงแทน FirstScreen

## ข้อตกลง
- ตอบ/คอมเมนต์เป็นภาษาไทยได้; ชื่อ class/method เป็นอังกฤษ
- เก็บ logic เกมใน `core` แยกจาก UI (ไม่ให้ GameManager/Player พึ่ง libGDX) เพื่อเทสได้และให้ bot ใช้ได้
- ผู้ใช้เป็นนักศึกษา ต้องนำเสนอโปรเจกต์ให้อาจารย์ -> โค้ดควรอ่านง่าย อธิบายได้
