<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Đăng nhập - PolyCoffee</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
        </head>

        <body>
            <div class="login-wrapper">
                <div class="login-card">
                    <div class="login-header">
                        <h1>PolyCoffee</h1>
                        <p>Đăng nhập hệ thống quản lý quán cà phê</p>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert-error">${error}</div>
                    </c:if>
                </div>
            </div>
        </body>