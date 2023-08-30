# socket-streamer

### 설명 
독립적으로 기능하도록 구현한 웹소켓(STOMP)서버

### 구현 방법 
Java11, Spring Boot2.7.8, redis, Gradle, STOMP
로컬 프로파일 일시에 -> 임베디드 레디스
운영 프로파일 일시에 -> 설정된 운영 레디스를 이용
병렬로 구성될 수 있도록 구현 
