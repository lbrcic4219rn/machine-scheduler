import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';

import { PermissionKey, Permissions, Session } from '../model';
import { ApiService } from './api.service';

const NO_PERMISSIONS: Permissions = {
  canReadUser: 0,
  canCreateUser: 0,
  canUpdateUser: 0,
  canDeleteUser: 0,
  canSearchMachine: 0,
  canStartMachine: 0,
  canStopMachine: 0,
  canRestartMachine: 0,
  canCreateMachine: 0,
  canDestroyMachine: 0
};

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  private readonly _session = signal<Session | null>(null);

  readonly session = this._session.asReadonly();
  readonly loggedIn = computed(() => this._session() !== null);
  readonly permissions = computed<Permissions>(() => this._session()?.permission ?? NO_PERMISSIONS);

  can(permission: PermissionKey): boolean {
    return this.permissions()[permission] === 1;
  }

  hasAnyPermission(): boolean {
    return Object.values(this.permissions()).includes(1);
  }

  login(email: string, password: string): Observable<Session> {
    return this.api.login({ email, password }).pipe(
      tap(session => this._session.set(session))
    );
  }

  loadSession(): Observable<boolean> {
    if (this._session() !== null) {
      return of(true);
    }
    return this.api.me().pipe(
      tap(session => this._session.set(session)),
      map(() => true),
      catchError(() => {
        this._session.set(null);
        return of(false);
      })
    );
  }

  logout(): void {
    this._session.set(null);
    this.api.logout().subscribe({
      next: () => void this.router.navigate(['/login']),
      error: () => void this.router.navigate(['/login'])
    });
  }

  clearSession(): void {
    this._session.set(null);
  }
}
