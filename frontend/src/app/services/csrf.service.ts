import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, shareReplay} from 'rxjs';

import {environment} from '../../environments/environment';
import {CsrfToken} from '../model';

@Injectable({
  providedIn: 'root'
})
export class CsrfService {

  private readonly http = inject(HttpClient);
  private token$: Observable<CsrfToken> | null = null;

  token(): Observable<CsrfToken> {
    this.token$ ??= this.http.get<CsrfToken>(`${environment.BASE_URL}/auth/csrf`).pipe(shareReplay(1));
    return this.token$;
  }
}
