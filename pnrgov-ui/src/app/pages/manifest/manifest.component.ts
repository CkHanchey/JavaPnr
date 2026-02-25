import { Component } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ApiService, ManifestResponse } from '../../services/api.service';

interface Passenger {
  name: string;
  documentType: string;
  documentNumber: string;
  nationality: string;
  seatNumber: string;
}

@Component({
  selector: 'app-manifest',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './manifest.component.html',
  styleUrl: './manifest.component.scss'
})
export class ManifestComponent {
  loading = false;
  error: string | null = null;
  response: ManifestResponse | null = null;
  passengers: Passenger[] = [];

  constructor(private apiService: ApiService) { }

  generateManifest(): void {
    this.loading = true;
    this.error = null;
    this.response = null;
    this.passengers = [];

    this.apiService.generateManifest().subscribe({
      next: (data) => {
        this.response = data;
        this.parseManifest(data.edifactContent);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to generate manifest. Please ensure the backend is running on port 8000.';
        this.loading = false;
        console.error('Error generating manifest:', err);
      }
    });
  }

  parseManifest(content: string): void {
    // Parse EDIFACT content to extract passenger information
    const lines = content.split('\n');
    const passengers: Passenger[] = [];
    
    let currentPassenger: Partial<Passenger> = {};
    
    for (const line of lines) {
      if (line.includes('NAD+FL+')) {
        // New passenger
        if (currentPassenger.name) {
          passengers.push(currentPassenger as Passenger);
        }
        currentPassenger = {};
        // Extract name (simplified parsing)
        const nameMatch = line.match(/NAD\+FL\+.*:([^']+)/);
        if (nameMatch) {
          currentPassenger.name = nameMatch[1];
        }
      } else if (line.includes('DOC+P') && currentPassenger.name) {
        // Document info
        const parts = line.split('+');
        if (parts.length > 2) {
          currentPassenger.documentType = 'Passport';
          const docParts = parts[2].split(':');
          currentPassenger.documentNumber = docParts[0] || 'N/A';
          currentPassenger.nationality = docParts[2] || 'N/A';
        }
      } else if (line.includes('LOC+') && currentPassenger.name) {
        // Could be seat info
        currentPassenger.seatNumber = currentPassenger.seatNumber || 'N/A';
      }
    }
    
    // Add last passenger
    if (currentPassenger.name) {
      passengers.push(currentPassenger as Passenger);
    }

    this.passengers = passengers;
  }

  downloadManifest(): void {
    if (this.response?.edifactContent) {
      const blob = new Blob([this.response.edifactContent], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `manifest_${this.response.generatedAt ?? 'download'}.txt`;
      a.click();
      window.URL.revokeObjectURL(url);
    }
  }

  printManifest(): void {
    window.print();
  }
}
