package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import javafx.scene.image.Image;

//Objeto que transmite os dados de um arquivo de mídia
public class UserFile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private byte[] bytes;

	public UserFile(File file) throws IOException {
		this.name = file.getName();
		this.bytes = getRawBytesFromFile(file.getAbsolutePath());
	}

	public UserFile() {
	};
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
	//Extrai os bytes de um arquivo
	private static byte[] getRawBytesFromFile(String path) throws IOException {
		byte[] image;
		File file = new File(path);
		image = new byte[(int)file.length()];
		
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			fileInputStream.read(image);
		}
		return image;
	}
	
	public record Scale(Double width, Double height) {}
	
	//Redimensiona a escala de uma imagem caso necessário
	public static Scale resizeScale(Image img) {
		Double width = img.getWidth();
		Double height = img.getHeight();
		
		while(width > 600 || height > 600) {
			width -= width * 0.1;
			height -= height * 0.1;
		}
		return new Scale(width, height);
	}
}
