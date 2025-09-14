import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { BasketService } from '../../services/basket.service';

@Component({
  selector: 'app-basket-display',
  templateUrl: './basket-display.component.html',
  styleUrls: ['./basket-display.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BasketDisplayComponent {
  protected readonly basketService = inject(BasketService);

  protected removeItem(sku: string): void {
    const basket = this.basketService.currentBasket();
    if (!basket) return;

    this.basketService.removeItemFromBasket(basket.id, sku).subscribe();
  }

  protected cancelBasket(): void {
    const basket = this.basketService.currentBasket();
    if (!basket) return;

    this.basketService.cancelBasket(basket.id).subscribe();
  }

  protected checkout(): void {
    const basket = this.basketService.currentBasket();
    if (!basket) return;

    this.basketService.checkout(basket.id).subscribe();
  }
}
