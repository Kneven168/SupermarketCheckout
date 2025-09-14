export interface Basket {
  id: string;
  items: BasketItem[]; // SKU -> quantity mapping
  totalPrice: number;
}

export interface BasketItem {
  sku: string;
  quantity: number;
}

export interface OrderItem {
  id?: number;
  orderId?: number;
  productSku: string;
  quantity: number;
}

export interface OrderDTO {
  id: number;
  finalPrice: number;
  items: OrderItem[];
  createdAt: string;
}
