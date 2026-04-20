<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NEXT DEBUT - 콘셉트 설정</title>
    
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Roboto:wght@300;400;700&display=swap" rel="stylesheet">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    
    <link rel="stylesheet" href="${ctx}/css/theme.css" />

    <style>
        body {
            font-family: "Roboto", sans-serif;
        }

        .font-orbitron {
            font-family: "Orbitron", sans-serif;
        }

        .gradient-text {
            background: linear-gradient(135deg, var(--pink), var(--lv));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .chip {
            background: rgba(255,255,255,0.42);
            border: 1px solid rgba(255,255,255,0.55);
            border-radius: 999px;
            box-shadow: 0 12px 30px rgba(0,0,0,0.08);
        }
    </style>
</head>
<body class="min-h-screen flex items-center justify-center px-6">
    <div class="max-w-4xl mx-auto text-center">
        <h1 class="font-orbitron text-5xl md:text-6xl font-black gradient-text mb-8">
            <i class="fas fa-palette"></i> 콘셉트 설정
        </h1>
        
        <div class="glass-card p-8 mb-8">
            <h2 class="font-orbitron text-3xl font-bold text-[rgba(44,44,44,0.92)] mb-6">
                선택된 연습생
            </h2>
            <div class="flex flex-wrap justify-center gap-4 mb-8">
                <c:forEach var="trainee" items="${selectedTrainees}">
                    <div class="chip px-6 py-3">
                        <span class="font-bold text-xl"><i class="fas fa-star"></i> ${trainee}</span>
                    </div>
                </c:forEach>
            </div>
            
            <p class="text-[rgba(44,44,44,0.70)] text-lg mb-6">
                축하합니다! 멋진 멤버들을 선택하셨네요. 🎉
            </p>
            
            <div class="text-[rgba(44,44,44,0.85)] text-xl font-bold">
                콘셉트 설정 기능은 준비 중입니다...
            </div>
        </div>
        
        <a href="${ctx}/main" class="btn-primary">
            <i class="fas fa-arrow-left"></i> 메인으로 돌아가기
        </a>
    </div>
</body>
</html>
