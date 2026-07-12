import { Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type AppLanguage = 'en' | 'fr';
const STORAGE_KEY = 'fb360_lang';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private _current = signal<AppLanguage>('en');
  readonly current = this._current.asReadonly();

  constructor(private translate: TranslateService) {
    this.translate.addLangs(['en', 'fr']);
    const saved = localStorage.getItem(STORAGE_KEY) as AppLanguage | null;
    const initial: AppLanguage = saved === 'fr' ? 'fr' : 'en';
    this.use(initial);
  }

  use(lang: AppLanguage): void {
    this._current.set(lang);
    this.translate.use(lang);
    localStorage.setItem(STORAGE_KEY, lang);
  }

  toggle(): void {
    this.use(this._current() === 'en' ? 'fr' : 'en');
  }
}
