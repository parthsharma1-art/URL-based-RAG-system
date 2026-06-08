package com.example.RagBasedApplication.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessDTO {
	private String message;

	public static SuccessDTO of(String message) {
		return new SuccessDTO(message);
	}

}
