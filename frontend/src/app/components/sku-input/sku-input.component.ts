import { Component, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BasketService } from '../../services/basket.service';

@Component({
  selector: 'app-sku-input',
  templateUrl: './sku-input.component.html',
  styleUrls: ['./sku-input.component.css'],
  imports: [FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SkuInputComponent {
  protected readonly basketService = inject(BasketService);
  protected readonly skuInput = signal<string>('');

  protected canAddToBasket(): boolean {
    return !this.basketService.loading() && 
           this.skuInput().trim().length > 0 && 
           this.basketService.hasBasket();
  }

  protected addToBasket(): void {
    const sku = this.skuInput().trim();
    const basket = this.basketService.currentBasket();
    
    if (!sku || !basket) return;

    this.basketService.addItemToBasket(basket.id, sku).subscribe(() => {
      this.skuInput.set(''); // Clear input after successful addition
    });
  }

  protected createBasket(): void {
    this.basketService.createBasket().subscribe();
  }
}
