package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class FileUtil {

	private static final Set <String> ALLOWED_EXTENSIONS = Set.of(
			 ".jpg", ".jpeg", ".png", ".gif", ".webp"
			);
	
	public static String upload(HttpServletRequest req, String fieldName, String folder)
	throws IOException, ServletException {
		Part part = req.getPart(fieldName);
		
		if(part == null || part.getSize() == 0) {
			return null;
		}
		
		String submittedFileName = part.getSubmittedFileName();
		if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
			return null;
		}
		
		String originalName = Paths.get(submittedFileName).getFileName().toString();
		String extension = getExtension(originalName).toLowerCase();
		
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException ("Chỉ chấp nhận file ảnh (.jpg, .jpeg, .png, .gif, .webp)!");
		}
		
		String newFileName = UUID.randomUUID().toString() + extension;
		String realPath = req.getServletContext().getRealPath(folder);
		File uploadDir = new File(realPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}
		
		File destination = new File(uploadDir, newFileName);
		part.write(destination.getAbsolutePath());
		return newFileName;
	}
	
	public static boolean delete(HttpServletRequest req, String folder, String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			return false;
		}
		
		String realPath = req.getServletContext().getRealPath(folder);
		File file = new File (realPath, fileName);
		if (file.exists() && file.isFile()) {
			return file.delete();
		}
		return false;
	}
	
	private static String getExtension (String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex < 0) {
			return "";
		}
		return fileName.substring(dotIndex);
	}
}

