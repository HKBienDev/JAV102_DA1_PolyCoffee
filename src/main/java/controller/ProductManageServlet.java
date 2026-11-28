package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import DAO.CategoryDAO;
import DAO.ProductDAO;
import entity.Category;
import entity.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.FileUtil;
import utils.ParamUtil;

@WebServlet({ "/product-manage", "/admin/product-manage" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class ProductManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_FOLDER = "/uploads/products";

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object msg = session.getAttribute("flashMessage");
            Object err = session.getAttribute("flashError");
            if (msg != null) {
                req.setAttribute("message", msg);
                session.removeAttribute("flashMessage");
            }
            if (err != null) {
                req.setAttribute("error", err);
                session.removeAttribute("flashError");
            }
        }

        try {
            String action = ParamUtil.getString(req, "action", "");
            if ("edit".equals(action)) {
                int id = ParamUtil.getInt(req, "id", 0);
                Product product = productDAO.findById(id);
                if (product != null) {
                    req.setAttribute("form", product);
                    req.setAttribute("formPrice", productDAO.getPriceByProductId(id));
                } else {
                    req.setAttribute("error", "Không tìm thấy sản phẩm #" + id);
                }
            }
            loadPageData(req);
        } catch (RuntimeException e) {
            e.printStackTrace();
            req.setAttribute("error", "Lỗi tải dữ liệu: " + e.getMessage());
        }

        req.getRequestDispatcher("/views/product-manage.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ParamUtil.getString(req, "action", "");
        try {
            switch (action) {
                case "create" -> handleCreate(req, resp);
                case "update" -> handleUpdate(req, resp);
                case "delete" -> handleDelete(req, resp);
                default -> resp.sendRedirect(getBaseUrl(req));
            }
        } catch (IllegalArgumentException e) {
            setFlashError(req, e.getMessage());
            resp.sendRedirect(getBaseUrl(req));
        } catch (RuntimeException e) {
            e.printStackTrace();
            setFlashError(req, "Thao tác thất bại: " + e.getMessage());
            resp.sendRedirect(getBaseUrl(req));
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = ParamUtil.getString(req, "name", "");
        int categoryId = ParamUtil.getInt(req, "categoryId", 0);
        double priceVal = ParamUtil.getDouble(req, "price", -1);
        String description = ParamUtil.getString(req, "description", "");
        boolean available = ParamUtil.getBoolean(req, "available", false);
        String rawImageUrl = ParamUtil.getString(req, "imageUrl", "");

        String error = validateProduct(name, categoryId, priceVal);
        if (error != null) {
            Product formProduct = Product.builder()
                    .categoryId(categoryId)
                    .name(name)
                    .description(description)
                    .imageUrl(rawImageUrl)
                    .available(available)
                    .build();
            req.setAttribute("error", error);
            req.setAttribute("form", formProduct);
            req.setAttribute("formPrice", priceVal >= 0 ? BigDecimal.valueOf(priceVal) : null);
            loadPageData(req);
            req.getRequestDispatcher("/views/product-manage.jsp").forward(req, resp);
            return;
        }

        String imageUrl = resolveImageUrl(req, null);
        Product newProduct = Product.builder()
                .categoryId(categoryId)
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .available(available)
                .build();

        int newId = productDAO.createReturningId(newProduct);
        productDAO.savePrice(newId, BigDecimal.valueOf(priceVal));

        setFlashMessage(req, "Đã thêm sản phẩm '" + name + "' thành công!");
        resp.sendRedirect(getBaseUrl(req));
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = ParamUtil.getInt(req, "id", 0);
        Product existing = productDAO.findById(id);
        if (existing == null) {
            setFlashError(req, "Vui lòng chọn sản phẩm cần sửa!");
            resp.sendRedirect(getBaseUrl(req));
            return;
        }

        String name = ParamUtil.getString(req, "name", "");
        int categoryId = ParamUtil.getInt(req, "categoryId", 0);
        double priceVal = ParamUtil.getDouble(req, "price", -1);
        String description = ParamUtil.getString(req, "description", "");
        boolean available = ParamUtil.getBoolean(req, "available", false);
        String rawImageUrl = ParamUtil.getString(req, "imageUrl", existing.getImageUrl());

        String error = validateProduct(name, categoryId, priceVal);
        if (error != null) {
            Product formProduct = Product.builder()
                    .id(id)
                    .categoryId(categoryId)
                    .name(name)
                    .description(description)
                    .imageUrl(rawImageUrl)
                    .available(available)
                    .build();
            req.setAttribute("error", error);
            req.setAttribute("form", formProduct);
            req.setAttribute("formPrice", priceVal >= 0 ? BigDecimal.valueOf(priceVal) : null);
            loadPageData(req);
            req.getRequestDispatcher("/views/product-manage.jsp").forward(req, resp);
            return;
        }

        String oldImage = existing.getImageUrl();
        String newImage = resolveImageUrl(req, oldImage);
        if (oldImage != null && !oldImage.equals(newImage) && oldImage.startsWith("uploads/products/")) {
            String oldFileName = oldImage.substring("uploads/products/".length());
            FileUtil.delete(req, UPLOAD_FOLDER, oldFileName);
        }

        Product updatedProduct = Product.builder()
                .id(id)
                .categoryId(categoryId)
                .name(name)
                .description(description)
                .imageUrl(newImage)
                .available(available)
                .build();

        productDAO.update(updatedProduct);
        productDAO.savePrice(id, BigDecimal.valueOf(priceVal));

        setFlashMessage(req, "Đã cập nhật sản phẩm #" + id + " thành công!");
        resp.sendRedirect(getBaseUrl(req));
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = ParamUtil.getInt(req, "id", 0);
        Product existing = productDAO.findById(id);
        if (existing == null) {
            setFlashError(req, "Không tìm thấy sản phẩm cần xóa!");
            resp.sendRedirect(getBaseUrl(req));
            return;
        }

        try {
            productDAO.delete(id);
            if (existing.getImageUrl() != null && existing.getImageUrl().startsWith("uploads/products/")) {
                String fileName = existing.getImageUrl().substring("uploads/products/".length());
                FileUtil.delete(req, UPLOAD_FOLDER, fileName);
            }
            setFlashMessage(req, "Đã xóa sản phẩm '" + existing.getName() + "'!");
        } catch (RuntimeException e) {
            setFlashError(req, "Không thể xóa vì sản phẩm đã có trong hóa đơn. Hãy chuyển sang Ngừng bán!");
        }
        resp.sendRedirect(getBaseUrl(req));
    }

    private void loadPageData(HttpServletRequest req) {
        List<Category> categories = categoryDAO.findAll();
        Map<Integer, String> categoryMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c.getName());
        }

        String keyword = ParamUtil.getString(req, "keyword", "");
        List<Product> products = keyword.isEmpty()
                ? productDAO.findAll()
                : productDAO.searchByName(keyword);

        req.setAttribute("categories", categories);
        req.setAttribute("categoryMap", categoryMap);
        req.setAttribute("products", products);
        req.setAttribute("priceMap", productDAO.getAllProductPrices());
    }

    private String resolveImageUrl(HttpServletRequest req, String oldImageUrl) throws IOException, ServletException {
        String contentType = req.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            String uploadedFileName = FileUtil.upload(req, "imageFile", UPLOAD_FOLDER);
            if (uploadedFileName != null) {
                return "uploads/products/" + uploadedFileName;
            }
        }
        String inputUrl = ParamUtil.getString(req, "imageUrl", "");
        return !inputUrl.isEmpty() ? inputUrl : (oldImageUrl != null ? oldImageUrl : "");
    }

    private String validateProduct(String name, int categoryId, double price) {
        if (name.isEmpty())
            return "Tên sản phẩm không được để trống!";
        if (categoryId <= 0)
            return "Vui lòng chọn phân loại sản phẩm!";
        if (price <= 0)
            return "Giá sản phẩm phải lớn hơn 0!";
        return null;
    }

    private String getBaseUrl(HttpServletRequest req) {
        return req.getContextPath() + req.getServletPath();
    }

    private void setFlashMessage(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flashMessage", message);
    }

    private void setFlashError(HttpServletRequest req, String error) {
        req.getSession(true).setAttribute("flashError", error);
    }
}
