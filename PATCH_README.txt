덮어쓰기 경로
- build.gradle
- .settings/org.eclipse.wst.common.project.facet.core.xml

적용 후 순서
1. 프로젝트 완전 종료
2. 파일 덮어쓰기
3. STS 실행
4. 프로젝트 우클릭 > Gradle > Refresh Gradle Project
5. Project > Clean
6. 필요하면 프로젝트 우클릭 > Refresh

변경 내용
- jakarta.servlet-api compileOnly 추가
- Eclipse Dynamic Web Module facet 2.4 -> 6.0 변경
