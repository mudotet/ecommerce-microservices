package com.mudotet.ecommerce.product.common;

public enum ErrorCode {
  PRODUCT_ALREADY_EXISTS("Product already exists"),
  CATEGORY_NOT_FOUND("Category not found");

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
