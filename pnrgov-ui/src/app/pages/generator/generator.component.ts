import { Component } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ApiService, EdifactResponse } from '../../services/api.service';

@Component({
  selector: 'app-generator',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './generator.component.html',
  styleUrl: './generator.component.scss'
})
export class GeneratorComponent {
  loading = false;
  error: string | null = null;
  response: EdifactResponse | null = null;

  constructor(private apiService: ApiService) { }

  generateEdifact(): void {
    this.loading = true;
    this.error = null;
    this.response = null;

    this.apiService.generateEdifact().subscribe({
      next: (data) => {
        this.response = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to generate EDIFACT message. Please ensure the backend is running on port 8000.';
        this.loading = false;
        console.error('Error generating EDIFACT:', err);
      }
    });
  }

  copyToClipboard(): void {
    if (this.response?.edifactContent) {
      navigator.clipboard.writeText(this.response.edifactContent).then(() => {
        alert('EDIFACT content copied to clipboard!');
      }).catch(err => {
        console.error('Failed to copy:', err);
      });
    }
  }

  downloadEdifact(): void {
    if (this.response?.edifactContent) {
      const blob = new Blob([this.response.edifactContent], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `edifact_${this.response.recordLocator}.txt`;
      a.click();
      window.URL.revokeObjectURL(url);
    }
  }
}
