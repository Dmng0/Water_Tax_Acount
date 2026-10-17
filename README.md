# 수도 계산기

여러 집의 월별 수도 요금을 한 번에 계산하는 Android 다크모드 앱 **수도계산기**입니다.

**[수도계산기 APK 바로 다운로드](https://github.com/Dmng0/Water_Tax_Acount/releases/latest/download/water-calculator.apk)**

최초 배포 전에는 다운로드 링크가 열리지 않습니다. GitHub Actions의 `Build and release APK`가 성공하면 APK가 자동으로 등록됩니다. Android 8.0 이상에서 설치할 수 있습니다.

## 사용 방법

1. 처음 실행하면 수도세 단가와 월 관리비를 입력합니다. 설정은 기기에 저장되며 상단 ‘요금 설정’에서 수정할 수 있습니다.
2. 처음에는 집 한 개가 표시됩니다. ‘+ 집 추가’로 늘리고, 각 집의 ‘삭제’로 제거합니다. 집 이름도 바꿀 수 있습니다.
3. 계산 월을 고르고 집마다 지난달·이번달 계량기 수치를 입력한 다음 ‘계산하고 기록 저장’을 누릅니다.
4. 왼쪽 상단 기록 버튼으로 사이드바를 엽니다. 기록을 누르면 당시 각 집의 이번달 수치가 현재 지난달 수치로 입력됩니다. 삭제했던 집은 같은 식별자로 복원됩니다. 기존 이번달 입력은 비워지고 계산 월은 기록의 다음 달로 바뀝니다.

계산식: **(이번달 계량기 수치 − 지난달 계량기 수치) × 수도세 단가 + 관리비**

단가와 관리비는 모든 집에 공통 적용합니다. 원 미만은 최종 합계에서 반올림합니다. 계량기 수치는 소수 3자리까지 지원하며 이번달이 지난달보다 작으면 계산하지 않습니다. 같은 월을 재계산하면 확인 후 별도 기록으로 저장하여 기존 내역도 보존합니다. 계산 버튼은 자동 월별 실행이 아닌 사용자가 매월 입력 후 누르는 방식입니다.

설정, 작성 중인 입력, 전체 계산 기록은 기기 내부에 저장합니다. 인터넷 권한이 없으며, 앱 데이터 삭제나 앱 제거 시 내역도 삭제됩니다. 집 삭제는 과거 기록에 영향을 주지 않습니다.

## 빌드 및 배포

JDK 17, Gradle 8.9, Android SDK 35 / Build Tools 35.0.0을 사용합니다.

```sh
gradle :app:assembleDebug
```

main 브랜치의 앱 변경 시 Actions에서 검증 후 디버그 APK를 아티팩트로 제공합니다. Actions → `Build and release APK` → Run workflow에서 `publish`를 선택하면 서명된 릴리스 APK를 GitHub Release에 등록합니다. `v*` 태그 푸시로도 배포됩니다.

배포 전에 저장소 Actions secrets에 다음을 등록하세요. 같은 키를 안전하게 보관하고 이후 버전에도 사용해야 기존 앱을 업데이트할 수 있습니다.

- `ANDROID_KEYSTORE_BASE64`: 아래 명령으로 만든 키스토어를 base64로 인코딩한 값
- `ANDROID_KEYSTORE_PASSWORD`: 키스토어와 키의 공통 비밀번호

```sh
keytool -genkeypair -keystore water-release.jks -alias water-calculator -keyalg RSA -keysize 3072 -validity 10000
base64 -w 0 water-release.jks
```

키 파일과 비밀번호는 저장소에 커밋하지 마세요. 서명 설정이 없으면 수동 배포는 실패하며, 디버그 APK 검증은 가능합니다. 릴리스 APK 이름은 `water-calculator.apk`로 고정됩니다.

로컬 계산 검증:

```sh
mkdir -p /tmp/water-tests
javac -d /tmp/water-tests app/src/main/java/kr/water/calculator/Billing.java tests/BillingTest.java
java -cp /tmp/water-tests kr.water.calculator.BillingTest
```
