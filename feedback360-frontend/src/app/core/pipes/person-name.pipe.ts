import { Pipe, PipeTransform } from '@angular/core';

function capitalizeFirst(value: string): string {
  if (!value) return '';
  const lower = value.trim().toLowerCase();
  return lower.charAt(0).toUpperCase() + lower.slice(1);
}

/** Displays a person's name as "LASTNAME Firstname". */
@Pipe({ name: 'personName', standalone: true })
export class PersonNamePipe implements PipeTransform {
  transform(firstName: string | null | undefined, lastName: string | null | undefined): string {
    const last = capitalizeFirst(lastName ?? '').toUpperCase();
    const first = capitalizeFirst(firstName ?? '');
    return `${last} ${first}`.trim();
  }
}
