import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  features = [
    {
      title: 'EDIFACT Generator',
      description: 'Generate PAXLST EDIFACT messages for passenger data transmission to government authorities.'
    },
    {
      title: 'Flight Manifest',
      description: 'Create comprehensive flight manifests with passenger, baggage, and seat information.'
    },
    {
      title: 'Sample Data',
      description: 'Automatically generates realistic sample data for testing and development purposes.'
    },
    {
      title: 'REST API',
      description: 'Modern REST API built with Spring Boot and Java 17 for reliable integration.'
    },
    {
      title: 'Angular Frontend',
      description: 'Responsive Angular 18 interface with modern design and user-friendly navigation.'
    },
    {
      title: 'SQLite Database',
      description: 'Lightweight embedded database for easy deployment and data management.'
    }
  ];

  techStack = [
    { name: 'Backend', value: 'Java 17 + Spring Boot 3.5.7' },
    { name: 'Frontend', value: 'Angular 18 + TypeScript 5.5' },
    { name: 'Database', value: 'SQLite 3.45.1' },
    { name: 'Build Tool', value: 'Gradle 7.6.4' },
    { name: 'ORM', value: 'Hibernate/JPA' },
    { name: 'Documentation', value: 'SpringDoc OpenAPI' }
  ];
}
