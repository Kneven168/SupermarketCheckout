import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { BasketService } from '../../services/basket.service';

@Component({
  selector: 'app-checkout-result',
  templateUrl: './checkout-result.component.html',
  styleUrls: ['./checkout-result.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutResultComponent {
  protected readonly basketService = inject(BasketService);

  protected startNewOrder(): void {
    this.basketService.clearLastOrder();
  }

  protected formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
