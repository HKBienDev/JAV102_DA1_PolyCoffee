<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quản Lý Sản Phẩm — PolyCoffee</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/product-manage.css">
            </head>

            <body>
                <div class="container">
                    <div class="page-header">
                        <div>
                            <h1>Quản Lý Sản Phẩm</h1>
                            <p class="subtitle">Thêm - Sửa - Xóa sản phẩm (Đầy đủ Tên, Giá, Hình ảnh, Phân loại)</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/product-manage" class="btn btn-reset">Tải lại danh
                            sách</a>
                    </div>

                    <c:if test="${not empty message}">
                        <div class="alert alert-success">${message}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <div class="card">
                        <h2>Thông tin sản phẩm</h2>
                        <form action="${pageContext.request.contextPath}/product-manage" method="post"
                            enctype="multipart/form-data">
                            <div class="form-grid">
                                <div class="form-group">
                                    <label for="id">ID sản phẩm</label>
                                    <input type="number" id="id" name="id" value="${form.id > 0 ? form.id : ''}"
                                        readonly placeholder="Tự tăng khi thêm mới">
                                </div>

                                <div class="form-group">
                                    <label for="categoryId">Phân loại sản phẩm</label>
                                    <select id="categoryId" name="categoryId" required>
                                        <option value="">-- Chọn danh mục --</option>
                                        <c:forEach var="cat" items="${categories}">
                                            <option value="${cat.id}" ${form.categoryId==cat.id ? 'selected' : '' }>
                                                ${cat.id} - ${cat.name}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="name">Tên sản phẩm</label>
                                    <input type="text" id="name" name="name" value="${form.name}"
                                        placeholder="Ví dụ: Cà phê sữa đá" required>
                                </div>

                                <div class="form-group">
                                    <label for="price">Giá bán (VNĐ)</label>
                                    <fmt:formatNumber var="plainPrice" value="${formPrice}" groupingUsed="false"
                                        maxFractionDigits="0" />
                                    <input type="number" id="price" name="price" value="${plainPrice}" min="1"
                                        step="any" placeholder="Ví dụ: 29000" required>
                                </div>

                                <div class="form-group">
                                    <label for="imageFile">Chọn file hình ảnh (từ máy tính)</label>
                                    <input type="file" id="imageFile" name="imageFile"
                                        accept=".jpg,.jpeg,.png,.gif,.webp">
                                </div>

                                <div class="form-group">
                                    <label for="imageUrl">Hoặc nhập đường dẫn ảnh (URL)</label>
                                    <input type="text" id="imageUrl" name="imageUrl" value="${form.imageUrl}"
                                        placeholder="Ví dụ: https://... hoặc giữ nguyên khi sửa">
                                </div>

                                <div class="form-group full-width">
                                    <label for="description">Mô tả sản phẩm</label>
                                    <textarea id="description" name="description" rows="2"
                                        placeholder="Nhập mô tả ngắn cho sản phẩm...">${form.description}</textarea>
                                </div>

                                <div class="form-group full-width">
                                    <div class="checkbox-row">
                                        <input type="checkbox" id="available" name="available" value="true" ${empty form
                                            || form.available ? 'checked' : '' }>
                                        <label for="available">Đang kinh doanh (Hiển thị trên menu bán hàng)</label>
                                    </div>
                                </div>
                            </div>

                            <div class="btn-group">
                                <button type="submit" name="action" value="create" class="btn btn-add">Thêm mới</button>
                                <button type="submit" name="action" value="update" class="btn btn-update">Cập
                                    nhật</button>
                                <c:if test="${not empty form and form.id > 0}">
                                    <button type="submit" name="action" value="delete" class="btn btn-delete"
                                        onclick="return confirm('Bạn có chắc muốn xóa sản phẩm này không?');">Xóa</button>
                                </c:if>
                                <a href="${pageContext.request.contextPath}/product-manage" class="btn btn-reset">Làm
                                    mới form</a>
                            </div>
                        </form>
                    </div>

                    <div class="card">
                        <div class="toolbar">
                            <h2 style="margin: 0;">Danh sách sản phẩm</h2>
                            <form action="${pageContext.request.contextPath}/product-manage" method="get"
                                class="search-form">
                                <input type="text" name="keyword" value="${param.keyword}"
                                    placeholder="Tìm theo tên sản phẩm...">
                                <button type="submit" class="btn btn-update btn-sm">Tìm</button>
                            </form>
                        </div>

                        <div class="table-wrapper">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Hình ảnh</th>
                                        <th>Tên sản phẩm</th>
                                        <th>Phân loại</th>
                                        <th>Giá bán</th>
                                        <th>Mô tả</th>
                                        <th>Trạng thái</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="p" items="${products}">
                                        <tr>
                                            <td>${p.id}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty p.imageUrl}">
                                                        <c:set var="imgSrc"
                                                            value="${p.imageUrl.startsWith('http') ? p.imageUrl : pageContext.request.contextPath.concat('/').concat(p.imageUrl)}" />
                                                        <img src="${imgSrc}" alt="${p.name}" class="thumb">
                                                    </c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><strong>${p.name}</strong></td>
                                            <td>${not empty categoryMap[p.categoryId] ? categoryMap[p.categoryId] :
                                                p.categoryId}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty priceMap[p.id] and priceMap[p.id] > 0}">
                                                        <strong>
                                                            <fmt:formatNumber value="${priceMap[p.id]}" type="number"
                                                                maxFractionDigits="0" /> đ
                                                        </strong>
                                                    </c:when>
                                                    <c:otherwise>Chưa có giá</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${p.description}</td>
                                            <td>
                                                <span class="badge ${p.available ? 'badge-active' : 'badge-inactive'}">
                                                    ${p.available ? 'Đang bán' : 'Ngừng bán'}
                                                </span>
                                            </td>
                                            <td class="actions-cell">
                                                <a href="${pageContext.request.contextPath}/product-manage?action=edit&id=${p.id}"
                                                    class="btn btn-update btn-sm">Sửa</a>

                                                <form action="${pageContext.request.contextPath}/product-manage"
                                                    method="post">
                                                    <input type="hidden" name="id" value="${p.id}">
                                                    <button type="submit" name="action" value="delete"
                                                        class="btn btn-delete btn-sm"
                                                        onclick="return confirm('Xóa sản phẩm #${p.id} (${p.name})?');">Xóa</button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty products}">
                                        <tr>
                                            <td colspan="8" class="empty-row">Chưa có sản phẩm nào trong danh sách.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </body>

            </html>