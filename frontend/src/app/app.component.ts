import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';

import {NavbarComponent} from './components/navbar/navbar.component';
import {ToastsComponent} from './components/toasts/toasts.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent, ToastsComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'machine-scheduler';
}
