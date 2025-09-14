import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { Basket, OrderDTO } from '../models/basket.model';

@Injectable({
  providedIn: 'root'
})
export class BasketService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/baskets';

  // Reactive state with signals
  private readonly _currentBasket = signal<Basket | null>(null);
  private readonly _loading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);
  private readonly _lastOrder = signal<OrderDTO | null>(null);

  // Public readonly signals
  readonly currentBasket = this._currentBasket.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly lastOrder = this._lastOrder.asReadonly();

  // Computed values
  readonly hasBasket = computed(() => this._currentBasket() !== null);
  readonly basketItems = computed(() => {
    const basket = this._currentBasket();
    if (!basket) return [];

    return Object.entries(basket.items).map(([sku, quantity]) => ({
      sku,
      quantity
    }));
  });
  readonly totalPrice = computed(() => this._currentBasket()?.totalPrice ?? 0);

  createBasket(): Observable<Basket> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.post<Basket>(this.baseUrl, {}).pipe(
      tap(basket => {
        this._currentBasket.set(basket);
        this._loading.set(false);
      }),
      catchError(error => {
        this._error.set('Failed to create basket');
        this._loading.set(false);
        return of();
      })
    );
  }

  getBasket(basketId: string): Observable<Basket> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.get<Basket>(`${this.baseUrl}/${basketId}`).pipe(
      tap(basket => {
        this._currentBasket.set(basket);
        this._loading.set(false);
      }),
      catchError(error => {
        this._error.set('Failed to load basket');
        this._loading.set(false);
        return of();
      })
    );
  }

  addItemToBasket(basketId: string, sku: string): Observable<number> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.post<number>(`${this.baseUrl}/${basketId}/items/${sku}`, {}).pipe(
      tap(() => {
        // Refresh basket after adding item
        this.getBasket(basketId).subscribe();
      }),
      catchError(error => {
        this._error.set('Failed to add item to basket');
        this._loading.set(false);
        return of(0);
      })
    );
  }

  removeItemFromBasket(basketId: string, sku: string): Observable<number> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.put<number>(`${this.baseUrl}/${basketId}/items/${sku}`, {}).pipe(
      tap(() => {
        // Refresh basket after removing item
        this.getBasket(basketId).subscribe();
      }),
      catchError(error => {
        this._error.set('Failed to remove item from basket');
        this._loading.set(false);
        return of(0);
      })
    );
  }

  cancelBasket(basketId: string): Observable<void> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.delete<void>(`${this.baseUrl}/${basketId}`).pipe(
      tap(() => {
        this._currentBasket.set(null);
        this._loading.set(false);
      }),
      catchError(error => {
        this._error.set('Failed to cancel basket');
        this._loading.set(false);
        return of();
      })
    );
  }

  checkout(basketId: string): Observable<OrderDTO> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.post<OrderDTO>(`${this.baseUrl}/${basketId}/checkout`, {}).pipe(
      tap(order => {
        this._lastOrder.set(order);
        this._currentBasket.set(null); // Clear basket after successful checkout
        this._loading.set(false);
      }),
      catchError(error => {
        this._error.set('Failed to checkout basket');
        this._loading.set(false);
        return of();
      })
    );
  }

  clearError(): void {
    this._error.set(null);
  }

  clearLastOrder(): void {
    this._lastOrder.set(null);
  }
}
