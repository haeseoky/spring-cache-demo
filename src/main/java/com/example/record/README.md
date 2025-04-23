# Java Record 예제 모음

이 프로젝트는 Java 14에서 preview로 소개되고 Java 16에서 정식으로 도입된 Record 타입의 다양한 사용 예제를 담고 있습니다.

## Record란?

Record는 불변(Immutable) 데이터 객체를 쉽게 생성하기 위한 Java의 특별한 클래스 유형입니다. 주로 데이터를 담기 위한 목적으로 설계되었으며, 다음과 같은 특징을 가집니다:

- 모든 필드는 `private final`로 자동 생성됨
- 생성자, getter 메서드, `equals()`, `hashCode()`, `toString()` 자동 생성
- 상속이 불가능 (다른 클래스를 상속할 수 없음)
- 인터페이스 구현 가능

## 예제 종류

이 패키지에는 다음과 같은 Record 예제가 포함되어 있습니다:

1. **기본 Record 예제** (`Person.java`)
   - 가장 기본적인 Record 정의 방법
   - 자동 생성되는 메서드 활용

2. **커스텀 생성자 Record** (`Employee.java`)
   - 유효성 검사 로직이 있는 컴팩트 생성자
   - 생성자 오버로딩 활용

3. **정적 팩토리 메서드 Record** (`Point.java`)
   - 다양한 객체 생성 방법 제공
   - 추가 메서드 정의

4. **불변 컬렉션 Record** (`Team.java`)
   - 방어적 복사를 통한 불변성 보장
   - 컬렉션 필드를 갖는 Record 활용

5. **중첩된 Record** (`Order.java`)
   - 계층적 데이터 구조 모델링
   - 내부에 다른 Record 정의 활용

6. **Optional 필드 Record** (`UserProfile.java`)
   - 선택적 필드를 갖는 Record
   - 다양한 생성자 활용

7. **열거형과 함께 사용하는 Record** (`Product.java`)
   - Record 내부에 enum 정의
   - 비즈니스 로직과 결합

8. **인터페이스 구현 Record** (`LogEntry.java`)
   - Serializable, Comparable 인터페이스 구현
   - 정적 팩토리 메서드 활용

9. **패턴 매칭과 함께 사용하는 Record** (`Shape.java`, `Circle.java`, `Rectangle.java`, `Triangle.java`, `ShapeCalculator.java`)
   - sealed 인터페이스와 Record 결합
   - Java 17+ 패턴 매칭 활용

10. **제네릭 Record** (`Pair.java`)
    - 타입 파라미터 활용
    - 유연한 데이터 구조 구현

11. **빌더 패턴 Record** (`BuildableMessage.java`)
    - 복잡한 객체 생성을 위한 빌더 패턴
    - 단계적 객체 생성 지원

12. **DTO Record** (`UserDTO.java`)
    - Spring과 같은 프레임워크에서 활용
    - 데이터 전송 객체로 활용 예시

13. **Record 활용 데모** (`RecordDemo.java`)
    - 모든 예제를 한 번에 실행하고 확인할 수 있는 테스트 클래스

## Record 사용의 장점

1. **간결성**: 상용구 코드(boilerplate code)를 줄여 코드 가독성 향상
2. **불변성**: 기본적으로 불변 객체로 생성되어 스레드 안전성 확보
3. **명확한 의도**: 단순히 데이터를 보유하는 객체임을 명확히 표현
4. **패턴 매칭 친화적**: Java 17+의 패턴 매칭과 결합 시 강력한 기능 발휘

## Record 사용이 적합한 상황

- 데이터 전송 객체(DTO)
- 값 객체(Value Object)
- 복합 키(Composite Key)
- API 응답/요청 모델
- 이벤트 메시지
- 함수형 프로그래밍에서의 불변 데이터 구조

## Record 사용이 적합하지 않은 상황

- 가변(Mutable) 상태가 필요한 경우
- 상속 계층이 필요한 경우
- 캡슐화된 데이터 변경이 필요한 경우
- 많은 추가 메서드나 비즈니스 로직이 필요한 경우

## 실행 방법

`RecordDemo` 클래스의 `main` 메서드를 실행하면 모든 예제의 동작을 확인할 수 있습니다.

## Java 버전 요구사항

- Java 16 이상 (Record 정식 지원)
- Java 17 이상 (패턴 매칭 예제 사용 시)
