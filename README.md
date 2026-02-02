# SideProject_MemberManager

홀덤펍 현장에서 고객 점수와 빙고 이벤트를 관리하기 위한 유저 관리 프로그램입니다.

## 문서

- **[프로그램 기획서](docs/프로그램_기획서.md)** — 요구사항, 기능, 화면 구성, 데이터 설계, 개발 일정 등

## 기술 스택

- **언어**: Java 17+
- **UI**: Swing
- **빌드**: Maven
- **저장**: JSON 파일 + 자동 백업 (Gson)
- **대상 환경**: Windows 10 이상

## 빌드 및 실행

- **필요 조건**: JDK 17 이상, Maven
- **빌드**: `mvn compile`
- **실행**: `mvn exec:java -q` 또는  
  `mvn package` 후 `java -jar target/member-manager-1.0.0-SNAPSHOT.jar`  
  (실행 시 `data/` 폴더에 JSON·백업 파일이 생성됩니다.)
- **최초 로그인**: 비밀번호 `admin` (최초 1회)

## 프로젝트 구조

```
src/main/java/com/holdempub/membermanager/
├── App.java                 # 진입점
├── domain/                  # 도메인 모델
│   ├── Member.java
│   └── BingoData.java
├── data/                    # JSON 저장·백업
│   ├── ConfigRepository.java
│   ├── MemberRepository.java
│   └── BingoRepository.java
├── util/
│   └── PasswordUtil.java    # SHA-256 비밀번호 해시
└── ui/
    ├── LoginFrame.java      # 로그인
    ├── MainFrame.java       # 메인(탭)
    ├── MainPanel.java       # 회원 목록·요약
    ├── MemberManagePanel.java  # 회원 추가/삭제
    ├── ScoreManagePanel.java   # 점수 추가
    └── BingoPanel.java      # 빙고판(가변 크기)
```

## 주요 기능

- 고객(닉네임) 등록·삭제
- 행동 기반 점수 추가·관리
- 가변 크기(2×2 ~ 6×6) 빙고판 관리 및 빙고/올빙고 판정
- 관리자 비밀번호 접근 제어
- 데이터 무결성 보장(분리 저장, 백업)
