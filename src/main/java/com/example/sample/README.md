# Java의 Sealed 클래스

이 디렉토리는 Java의 sealed 클래스와 인터페이스 예제를 포함하고 있습니다.

## Sealed 클래스란 무엇인가?

Sealed 클래스는 Java 17에서 프리뷰 기능으로 도입되었으며 Java 21에서 표준이 되었습니다. 이 기능은 다른 클래스나 인터페이스가 자신을 확장하거나 구현할 수 있는지 제한할 수 있게 해줍니다.

Sealed 클래스나 인터페이스는 명시적으로 허용된 클래스와 인터페이스만 확장하거나 구현할 수 있습니다. 이는 전혀 확장할 수 없는 final 클래스와 어떤 클래스든 확장할 수 있는 일반 클래스 사이의 중간 지점을 제공합니다.

## Sealed 클래스의 장점

1. **제어된 상속**: 어떤 클래스가 당신의 클래스를 확장하거나 인터페이스를 구현할 수 있는지 명시적으로 제어할 수 있습니다.
2. **패턴 매칭**: switch 표현식에서 완전한 패턴 매칭을 가능하게 합니다.
3. **더 나은 API 설계**: 라이브러리 설계자가 자신의 API를 확장할 수 있는 클래스를 제한할 수 있습니다.
4. **문서화**: 클래스 계층 구조를 명시적이고 자체 문서화되도록 만듭니다.

## 이 디렉토리의 예제

1. **Shape 예제**: 기본적인 sealed 클래스와 그 하위 클래스(Circle, Rectangle, Triangle)를 보여줍니다.
2. **Vehicle 예제**: 중첩된 sealed 클래스 계층 구조를 보여줍니다. Vehicle(sealed 클래스) → Car(sealed 클래스) → Sedan, SUV(final 클래스) 및 Motorcycle(final 클래스), Truck(non-sealed 클래스) 구조를 갖습니다.
3. **HttpResponse 예제**: sealed 인터페이스와 그 구현체(SuccessResponse, ErrorResponse, RedirectResponse)를 보여줍니다.
4. **Operation 예제**: 패턴 매칭과 함께 사용하는 sealed 클래스를 보여줍니다. Calculator 클래스에서 Java 17+ switch 표현식의 패턴 매칭 기능을 활용합니다.
5. **PaymentResult 예제**: Spring 환경에서 sealed 클래스를 사용하는 예제를 보여줍니다. PaymentService 클래스에서 패턴 매칭을 활용한 응답 처리를 보여줍니다.
6. **SealedClassDemo.java**: 모든 예제를 통합하여 실행하는 메인 클래스입니다. switch 표현식과 패턴 매칭을 활용한 예시를 포함합니다.

## 주요 개념

- sealed 클래스나 인터페이스는 `sealed` 수정자를 사용해야 하며, 확장하거나 구현할 수 있는 모든 클래스를 나열하는 `permits` 절을 포함해야 합니다.
- 허용된 클래스는 다음 중 하나여야 합니다:
  - `final` (더 이상 확장할 수 없음)
  - `sealed` (자신의 허용된 하위 클래스만 확장 가능)
  - `non-sealed` (어떤 클래스든 확장 가능)
- 모든 허용된 클래스는 sealed 클래스와 동일한 패키지나 모듈에 있어야 합니다.

## 예제 실행하기

예제를 실행하려면 `SealedClassDemo` 클래스를 실행하세요:

```bash
java com.example.sample.SealedClassDemo
```

이 데모는 다음과 같은 예제들을 보여줍니다:

1. **Shape 예제**: Circle, Rectangle, Triangle 등의 다양한 도형에 대한 sealed 클래스 구현
2. **Vehicle 예제**: Sedan, SUV, Motorcycle, Truck 등을 포함하는 중첩된 sealed 클래스 계층 구조
3. **HttpResponse 예제**: SuccessResponse, ErrorResponse, RedirectResponse 등의 HTTP 응답 구현
4. **Operation 예제**: 사칙연산 기능을 구현한 sealed 클래스 및 Calculator 클래스의 패턴 매칭 사용
5. **PaymentResult 예제**: Spring 환경에서의 결제 처리를 모델링한 sealed 클래스 및 패턴 매칭을 활용한 처리

각 예제에서는 다음 같은 기능을 살펴볼 수 있습니다:

- sealed 클래스와 인터페이스의 인스턴스 생성 방법
- pattern matching을 사용한 타입 안전한 코드 작성
- Java 17+ 의 switch 표현식을 사용한 패턴 매칭
- sealed 클래스의 확장성과 유연성

## Java 버전 요구사항

이 예제들은 컴파일 및 실행을 위해 Java 17 이상이 필요합니다. switch 표현식의 패턴 매칭과 같은 일부 기능은 완전한 지원을 위해 Java 21이 필요할 수 있습니다.

## Sealed 클래스 vs Enum 비교

### Sealed 클래스 장점
1. **타입 계층 구조 제한**: 상속 가능한 클래스를 명시적으로 제한하여 타입 안전성 향상
2. **패턴 매칭 최적화**: Java 17+ 버전의 패턴 매칭과 함께 사용할 때 컴파일러가 더 효율적인 최적화 가능
3. **확장성**: enum보다 더 복잡한 객체 모델과 행동을 구현 가능
4. **인스턴스별 메서드 구현**: 각 서브클래스마다 다른 메서드 구현 제공 가능
5. **상태 저장**: 각 인스턴스마다 다른 상태 저장 가능

### Enum 장점
1. **단순성**: 구현이 간단하고 직관적
2. **타입 안전성**: 컴파일 타임에 타입 체크 제공
3. **싱글톤 보장**: 각 상수는 JVM에서 하나의 인스턴스만 존재
4. **직렬화 지원**: 기본적으로 잘 지원됨
5. **switch 문 최적화**: 컴파일러가 switch 문에서 최적화 제공
6. **값 열거**: values() 메서드로 모든 값을 쉽게 열거 가능
7. **하위 버전 호환성**: Java 5 이상에서 사용 가능

### 사용 권장 상황

**Sealed Class 사용**:
- 복잡한 도메인 모델이 필요할 때
- 각 케이스마다 다른 상태와 행동이 필요할 때
- 계층 구조를 제한하면서도 유연성이 필요할 때
- 타입 계층 구조를 명확하게 정의하고자 할 때

**Enum 사용**:
- 단순한 상수 집합이 필요할 때
- 고정된 값 세트가 필요하고 변화가 적을 때
- 하위 버전 호환성이 중요할 때
- 코드 단순성이 중요할 때
