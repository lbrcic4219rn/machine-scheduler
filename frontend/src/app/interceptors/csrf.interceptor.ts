import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {switchMap} from 'rxjs';

import {CsrfService} from '../services/csrf.service';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

// Angular's built-in XSRF support skips absolute URLs, and the API lives on another port.
export const csrfInterceptor: HttpInterceptorFn = (request, next) => {
  if (SAFE_METHODS.has(request.method)) {
    return next(request);
  }
  return inject(CsrfService).token().pipe(
    switchMap(csrf => next(request.clone({setHeaders: {[csrf.headerName]: csrf.token}})))
  );
};
