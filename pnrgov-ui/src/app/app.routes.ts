import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { GeneratorComponent } from './pages/generator/generator.component';
import { ManifestComponent } from './pages/manifest/manifest.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'generator', component: GeneratorComponent },
  { path: 'manifest', component: ManifestComponent },
  { path: '**', redirectTo: '' }
];
