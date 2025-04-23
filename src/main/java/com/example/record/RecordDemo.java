package com.example.record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Record 사용 예제를 보여주는 데모 클래스
 * - 다양한 Record 활용 방법 시연
 */
public class RecordDemo {

    public static void main(String[] args) {
        demonstrateBasicRecord();
        demonstrateEmployeeRecord();
        demonstratePointRecord();
        demonstrateTeamRecord();
        demonstrateOrderRecord();
        demonstrateUserProfileRecord();
        demonstrateProductRecord();
        demonstrateLogEntryRecord();
        demonstrateShapeRecords();
        demonstrateGenericPairRecord();
        demonstrateBuildableMessageRecord();
        demonstrateUserDTORecord();
    }

    private static void demonstrateBasicRecord() {
        System.out.println("\n===== 기본 Record 예제 =====");
        
        Person person = new Person("홍길동", 30, "hong@example.com");
        System.out.println("Person 객체: " + person);
        System.out.println("이름: " + person.name());
        System.out.println("나이: " + person.age());
        System.out.println("이메일: " + person.email());
        
        // equals() 테스트
        Person samePerson = new Person("홍길동", 30, "hong@example.com");
        Person differentPerson = new Person("김철수", 25, "kim@example.com");
        
        System.out.println("같은 사람과 equals: " + person.equals(samePerson));
        System.out.println("다른 사람과 equals: " + person.equals(differentPerson));
    }

    private static void demonstrateEmployeeRecord() {
        System.out.println("\n===== Employee Record 예제 =====");
        
        try {
            Employee employee1 = new Employee("E001", "홍길동", 50000.0);
            System.out.println("유효한 직원: " + employee1);
            
            Employee employee2 = new Employee("E002", "김철수");
            System.out.println("기본 급여 직원: " + employee2);
            
            // 예외 발생 케이스
            Employee invalidEmployee = new Employee("", "이영희", 30000.0);
            System.out.println("이 줄은 실행되지 않아야 합니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("예상대로 예외 발생: " + e.getMessage());
        }
        
        try {
            // 예외 발생 케이스
            Employee negSalaryEmployee = new Employee("E003", "박지성", -1000.0);
            System.out.println("이 줄은 실행되지 않아야 합니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("예상대로 예외 발생: " + e.getMessage());
        }
    }

    private static void demonstratePointRecord() {
        System.out.println("\n===== Point Record 예제 =====");
        
        Point p1 = new Point(3, 4);
        System.out.println("좌표: " + p1);
        System.out.println("원점까지의 거리: " + p1.distanceFromOrigin());
        
        Point origin = Point.origin();
        System.out.println("원점: " + origin);
        
        Point p2 = Point.fromPolar(5, Math.PI / 4); // 반지름 5, 45도 각도
        System.out.println("극좌표에서 변환된 점: " + p2);
        System.out.println("두 점 사이의 거리: " + p1.distanceTo(p2));
    }

    private static void demonstrateTeamRecord() {
        System.out.println("\n===== Team Record 예제 =====");
        
        List<String> membersList = List.of("홍길동", "김철수", "이영희");
        Team team = new Team("개발팀", membersList);
        
        System.out.println("팀 정보: " + team);
        System.out.println("팀 크기: " + team.size());
        System.out.println("김철수는 팀원인가?: " + team.hasMember("김철수"));
        System.out.println("박지성은 팀원인가?: " + team.hasMember("박지성"));
        
        // 불변성 테스트
        List<String> retrievedMembers = team.getMembers();
        System.out.println("조회된 팀원 목록: " + retrievedMembers);
        
        try {
            // UnsupportedOperationException 예상
            retrievedMembers.add("박지성");
            System.out.println("이 줄은 실행되지 않아야 합니다.");
        } catch (UnsupportedOperationException e) {
            System.out.println("예상대로 불변 컬렉션은 수정할 수 없습니다.");
        }
    }

    private static void demonstrateOrderRecord() {
        System.out.println("\n===== Order Record 예제 =====");
        
        Order.Customer customer = new Order.Customer("C001", "홍길동", "서울시 강남구");
        
        List<Order.OrderItem> items = List.of(
            new Order.OrderItem("P001", "노트북", 1, 1500000.0),
            new Order.OrderItem("P002", "마우스", 2, 35000.0),
            new Order.OrderItem("P003", "키보드", 1, 89000.0)
        );
        
        Order order = new Order("ORD001", customer, items);
        
        System.out.println("주문 정보: " + order);
        System.out.println("총 금액: " + order.getTotalAmount() + "원");
        System.out.println("총 상품 수량: " + order.getTotalQuantity() + "개");
        
        // 불변성 테스트
        List<Order.OrderItem> retrievedItems = order.items();
        try {
            // UnsupportedOperationException 예상
            retrievedItems.add(new Order.OrderItem("P004", "헤드폰", 1, 120000.0));
            System.out.println("이 줄은 실행되지 않아야 합니다.");
        } catch (UnsupportedOperationException e) {
            System.out.println("예상대로 불변 컬렉션은 수정할 수 없습니다.");
        }
    }

    private static void demonstrateUserProfileRecord() {
        System.out.println("\n===== UserProfile Record 예제 =====");
        
        UserProfile basicProfile = new UserProfile("user123", "홍길동");
        System.out.println("기본 프로필: " + basicProfile);
        System.out.println("완성된 프로필?: " + basicProfile.hasCompletedProfile());
        
        UserProfile profileWithBio = new UserProfile("user456", "김철수", "안녕하세요! 개발자입니다.");
        System.out.println("바이오가 있는 프로필: " + profileWithBio);
        System.out.println("완성된 프로필?: " + profileWithBio.hasCompletedProfile());
        
        UserProfile fullProfile = new UserProfile("user789", "이영희", 
                                                 Optional.of("백엔드 개발자"), 
                                                 Optional.of("https://example.com"));
        System.out.println("전체 프로필: " + fullProfile);
        System.out.println("완성된 프로필?: " + fullProfile.hasCompletedProfile());
        
        System.out.println("\n프로필 요약:");
        System.out.println(fullProfile.getSummary());
    }

    private static void demonstrateProductRecord() {
        System.out.println("\n===== Product Record 예제 =====");
        
        Product laptop = new Product("P001", "고성능 노트북", 
                                    Product.Category.ELECTRONICS, 1500000.0);
        Product shirt = new Product("P002", "면 티셔츠", 
                                   Product.Category.CLOTHING, 30000.0);
        Product apple = new Product("P003", "사과 1kg", 
                                   Product.Category.FOOD, 8000.0);
        Product book = new Product("P004", "자바 프로그래밍", 
                                  Product.Category.BOOKS, 25000.0);
        Product misc = new Product("P005", "기타 물품", 
                                  Product.Category.OTHER, 10000.0);
        
        System.out.println("노트북 원가: " + laptop.price() + "원");
        System.out.println("노트북 할인가: " + laptop.getDiscountedPrice() + "원");
        System.out.println("노트북 세금 포함 가격: " + laptop.getPriceWithTax() + "원");
        
        System.out.println("\n모든 제품의 할인가:");
        printDiscountedPrice(laptop);
        printDiscountedPrice(shirt);
        printDiscountedPrice(apple);
        printDiscountedPrice(book);
        printDiscountedPrice(misc);
    }
    
    private static void printDiscountedPrice(Product product) {
        double discount = product.price() - product.getDiscountedPrice();
        double discountRate = product.category().getDiscountRate() * 100;
        
        System.out.printf("%s: %,.0f원 (할인율: %.0f%%, 할인액: %,.0f원)%n", 
                         product.name(), product.getDiscountedPrice(), 
                         discountRate, discount);
    }

    private static void demonstrateLogEntryRecord() {
        System.out.println("\n===== LogEntry Record 예제 =====");
        
        LogEntry info = new LogEntry("LOG001", LocalDateTime.now(), LogEntry.INFO, 
                                    "사용자 로그인 성공");
        LogEntry warning = new LogEntry("LOG002", LocalDateTime.now().plusMinutes(5), 
                                       LogEntry.WARNING, "API 응답 지연");
        LogEntry error = new LogEntry("LOG003", LocalDateTime.now().plusMinutes(10), 
                                     LogEntry.ERROR, "데이터베이스 연결 실패");
        
        System.out.println("정보 로그: " + info.getFormattedLog());
        System.out.println("경고 로그: " + warning.getFormattedLog());
        System.out.println("오류 로그: " + error.getFormattedLog());
        
        System.out.println("\n로그 타입 확인:");
        System.out.println("정보 로그가 오류인가?: " + info.isError());
        System.out.println("오류 로그가 오류인가?: " + error.isError());
        
        // Comparable 테스트
        List<LogEntry> logs = List.of(error, info, warning);
        List<LogEntry> sortedLogs = logs.stream().sorted().toList();
        
        System.out.println("\n시간순 정렬된 로그:");
        sortedLogs.forEach(log -> System.out.println("- " + log.getFormattedLog()));
        
        // 정적 팩토리 메서드 테스트
        LogEntry factoryInfo = LogEntry.info("팩토리 메서드로 생성된 로그");
        LogEntry factoryError = LogEntry.error("오류가 발생했습니다");
        
        System.out.println("\n팩토리 메서드로 생성된 로그:");
        System.out.println(factoryInfo.getFormattedLog());
        System.out.println(factoryError.getFormattedLog());
    }

    private static void demonstrateShapeRecords() {
        System.out.println("\n===== Shape Record 예제 =====");
        
        Circle circle = new Circle(5.0);
        Rectangle rectangle = new Rectangle(4.0, 6.0);
        Rectangle square = new Rectangle(5.0, 5.0);
        Triangle equilateral = new Triangle(4.0, 4.0, 4.0);
        Triangle scalene = new Triangle(3.0, 4.0, 5.0);
        
        System.out.println("도형 설명:");
        System.out.println("- " + ShapeCalculator.describeShape(circle));
        System.out.println("- " + ShapeCalculator.describeShape(rectangle));
        System.out.println("- " + ShapeCalculator.describeShape(square));
        System.out.println("- " + ShapeCalculator.describeShape(equilateral));
        System.out.println("- " + ShapeCalculator.describeShape(scalene));
        
        System.out.println("\n도형 면적 (자체 메서드):");
        System.out.println("원의 면적: " + circle.area());
        System.out.println("직사각형의 면적: " + rectangle.area());
        System.out.println("정삼각형의 면적: " + equilateral.area());
        
        System.out.println("\n도형 면적 (계산기 사용):");
        System.out.println("원의 면적: " + ShapeCalculator.calculateArea(circle));
        System.out.println("직사각형의 면적: " + ShapeCalculator.calculateArea(rectangle));
        System.out.println("삼각형의 면적: " + ShapeCalculator.calculateArea(scalene));
        
        System.out.println("\n도형 특성:");
        System.out.println("원의 지름: " + circle.diameter());
        System.out.println("직사각형이 정사각형인가?: " + rectangle.isSquare());
        System.out.println("정사각형이 정사각형인가?: " + square.isSquare());
        System.out.println("직사각형의 대각선 길이: " + rectangle.diagonal());
        System.out.println("삼각형의 유형: " + equilateral.getType());
        System.out.println("삼각형의 유형: " + scalene.getType());
    }

    private static void demonstrateGenericPairRecord() {
        System.out.println("\n===== Pair<K, V> Record 예제 =====");
        
        Pair<String, Integer> nameAge = new Pair<>("홍길동", 30);
        System.out.println("이름-나이 쌍: " + nameAge);
        
        Pair<Integer, String> idName = nameAge.swap();
        System.out.println("교체된 쌍: " + idName);
        
        Pair<String, Integer> updatedAge = nameAge.withValue(31);
        System.out.println("값 갱신된 쌍: " + updatedAge);
        
        Pair<String, Integer> factory = Pair.of("김철수", 25);
        System.out.println("팩토리 메서드로 생성된 쌍: " + factory);
        
        Pair<Integer, Integer> empty = Pair.empty(0);
        System.out.println("기본값으로 생성된 쌍: " + empty);
        
        // 실제 애플리케이션 사용 예
        Pair<String, Double> exchangeRate = new Pair<>("USD", 1350.50);
        System.out.println("\n환율 정보: 1 " + exchangeRate.key() + " = " 
                          + exchangeRate.value() + " KRW");
    }

    private static void demonstrateBuildableMessageRecord() {
        System.out.println("\n===== BuildableMessage Record 예제 =====");
        
        BuildableMessage simpleMessage = BuildableMessage.builder()
            .sender("sender@example.com")
            .recipient("recipient@example.com")
            .build();
        
        System.out.println("간단한 메시지:\n" + simpleMessage.format());
        
        BuildableMessage fullMessage = BuildableMessage.builder()
            .sender("hong@example.com")
            .recipient("kim@example.com")
            .subject("회의 일정 안내")
            .body("안녕하세요,\n다음 주 월요일 오전 10시에 회의가 예정되어 있습니다.\n참석 부탁드립니다.")
            .urgent(true)
            .addAttachment("meeting_agenda.pdf")
            .addAttachment("location_map.png")
            .build();
            
        System.out.println("\n전체 메시지:\n" + fullMessage.format());
        
        try {
            BuildableMessage invalidMessage = BuildableMessage.builder()
                .subject("제목만 있는 메시지")
                .build();
            System.out.println("이 줄은 실행되지 않아야 합니다.");
        } catch (IllegalStateException e) {
            System.out.println("\n예상대로 예외 발생: " + e.getMessage());
        }
    }

    private static void demonstrateUserDTORecord() {
        System.out.println("\n===== UserDTO Record 예제 =====");
        
        UserDTO.RoleDTO adminRole = new UserDTO.RoleDTO(1L, "ADMIN");
        UserDTO.RoleDTO userRole = new UserDTO.RoleDTO(2L, "USER");
        
        UserDTO user1 = new UserDTO(101L, "hong", "hong@example.com", 
                                  LocalDateTime.now().minusDays(30));
        
        UserDTO user2 = new UserDTO(102L, "kim", "kim@example.com", 
                                  LocalDateTime.now().minusDays(15),
                                  List.of(adminRole, userRole));
        
        System.out.println("기본 사용자: " + user1);
        System.out.println("역할이 있는 사용자: " + user2);
        
        System.out.println("\n사용자 요약:");
        System.out.println("- " + user1.getSummary());
        System.out.println("- " + user2.getSummary());
        
        // 실제 애플리케이션에서는 다음과 같이 사용할 수 있습니다.
        // Spring Controller에서:
        // @GetMapping("/users/{id}")
        // public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        //     User user = userService.findById(id);
        //     return ResponseEntity.ok(UserDTO.fromEntity(user));
        // }
    }
}
