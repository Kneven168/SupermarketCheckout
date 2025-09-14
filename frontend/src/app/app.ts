import { Component, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SkuInputComponent } from './components/sku-input/sku-input.component';
import { BasketDisplayComponent } from './components/basket-display/basket-display.component';
import { CheckoutResultComponent } from './components/checkout-result/checkout-result.component';
import { BasketService } from './services/basket.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SkuInputComponent, BasketDisplayComponent, CheckoutResultComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Supermarket Checkout');
  protected readonly basketService = inject(BasketService);
}
