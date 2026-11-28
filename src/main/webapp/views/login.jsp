<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Đăng nhập — PolyCoffee</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/login.css">
        </head>

        <body>
            <div class="login-wrapper">
                <div class="login-card">
                    <div class="brand-header">
                        <div class="brand-icon">&#9749;</div>
                        <h1>PolyCoffee</h1>
                        <p>Đăng nhập hệ thống quản lý quán cà phê</p>
                    </div>

                    <%-- Hiển thị thông báo lỗi do LoginServlet gửi sang qua req.setAttribute("error", ...) --%>
                        <c:if test="${not empty error}">
                            <div class="alert alert-error">
                                <c:out value="${error}" />
                            </div>
                        </c:if>

                        <%-- Hiển thị thông báo khi vừa đăng xuất (nếu URL có ?logout=success) --%>
                            <c:if test="${param.logout == 'success'}">
                                <div class="alert alert-success">
                                    Bạn đã đăng xuất khỏi hệ thống.
                                </div>
                            </c:if>

                            <form action="${pageContext.request.contextPath}/login" method="post" id="loginForm">
                                <div class="form-group">
                                    <label for="username">Tên đăng nhập</label>
                                    <input type="text" id="username" name="username" class="input-control"
                                        value="<c:out value='${username}'/>" placeholder="Nhập tên đăng nhập..."
                                        autocomplete="username" required autofocus>
                                </div>

                                <div class="form-group">
                                    <label for="password">Mật khẩu</label>
                                    <div class="password-box">
                                        <input type="password" id="password" name="password" class="input-control"
                                            placeholder="Nhập mật khẩu..." autocomplete="current-password" required>
                                        <button type="button" class="btn-toggle-pw" id="togglePwBtn"
                                            onclick="togglePassword()">
                                            Hiện
                                        </button>
                                    </div>
                                </div>

                                <button type="submit" class="btn-submit" id="submitBtn">Đăng nhập</button>
                            </form>

                            <div class="login-footer">
                                <a href="${pageContext.request.contextPath}/">&larr; Quay về trang chủ</a>
                            </div>
                </div>
            </div>

            <script>
                function togglePassword() {
                    const pwInput = document.getElementById("password");
                    const toggleBtn = document.getElementById("togglePwBtn");
                    if (pwInput.type === "password") {
                        pwInput.type = "text";
                        toggleBtn.textContent = "Ẩn";
                    } else {
                        pwInput.type = "password";
                        toggleBtn.textContent = "Hiện";
                    }
                }

                document.getElementById("loginForm").addEventListener("submit", function () {
                    const btn = document.getElementById("submitBtn");
                    btn.disabled = true;
                    btn.textContent = "Đang kiểm tra...";
                });
            </script>
        </body>

        </html>