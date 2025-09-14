export interface Basket {
  id: string;
  items: Record<string, number>; // SKU -> quantity mapping
  totalPrice: number;
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
