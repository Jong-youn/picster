# picster
이미지 관리 서비스

### API
- 이미지 업로드: 클라이언트로부터 받은 이미지 URL을 다운받아 저장매체에 사용자 정보와 함께 저장
  - 이미지는 로컬 스토리지에 저장 src/main/resources/storage/
  - 이미지 정보는 데이터베이스에 저장
- 이미지 정보 변경
- 이미지 삭제: 클라이언트가 업로드한 이미지를 소프트 딜리트
- 이미지 목록 조회: 이미지 목록을 페이지네이션으로 반환
- 이미지 상세 조회: 이미지 상세정보를 반환하고 조회수 증가

### SKILL
- Spring Boot 3.0
- Java 21
- Ehcache 3
- MySQL
- Lombok
- Junit 5
- Docker

### 실행방법
```
gh repo clone Jong-youn/picster
cd picster
docker-compose up
```
