import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// ── Shared sub-schemas ────────────────────────────────────────────────────────

export interface FileOptions {
  hasBags: boolean;
  hasSeats: boolean;
  hasDocuments: boolean;
  hasPayment: boolean;
  isCodeshare: boolean;
  isThruFlight: boolean;
  hasPhones: boolean;
  hasAgency: boolean;
  hasCreditCard: boolean;
}

export interface GeneratedFile {
  fileName: string;
  recordLocator: string;
  content: string;
  passengerCount: number;
  flightCount: number;
  options: FileOptions;
}

export interface TravelDocument {
  id: number;
  passengerId: number;
  documentType: string;
  documentNumber: string;
  issuingCountry: string;
  expiryDate: string;
  issueDate: string;
  nationality: string;
}

export interface Baggage {
  id: number;
  passengerId: number;
  flightId: number;
  bagTagNumber: string;
  weight: number;
  weightUnit: string;
  numberOfPieces: number;
  baggageType: string;
  status: string;
}

export interface SeatAssignment {
  id: number;
  passengerId: number;
  flightId: number;
  seatNumber: string;
  seatCharacteristics: string;
}

export interface Passenger {
  id: number;
  firstName: string;
  lastName: string;
  middleName?: string;
  title?: string;
  dateOfBirth: string;
  gender: string;
  nationality: string;
  passengerType: string;
  email?: string;
  phone?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  documents: TravelDocument[];
  bags: Baggage[];
  seats: SeatAssignment[];
}

export interface Flight {
  id: number;
  flightNumber: string;
  airlineCode: string;
  departureAirport: string;
  arrivalAirport: string;
  departureDate: string;
  arrivalDate: string;
  aircraftType?: string;
  serviceClass?: string;
  operatingCarrier?: string;
  flightStatus?: string;
  segmentNumber: number;
}

export interface Payment {
  id: number;
  reservationId: number;
  paymentType: string;
  cardType?: string;
  cardNumber?: string;
  expiryDate?: string;
  cardHolderName?: string;
  amount: number;
  currency: string;
  paymentDate: string;
}

export interface Reservation {
  id: number;
  recordLocator: string;
  bookingDate: string;
  createdDate: string;
  bookingChannel?: string;
  agencyCode?: string;
  status?: string;
  contactFirstName?: string;
  contactLastName?: string;
  contactEmail?: string;
  contactPhone?: string;
  passengers: Passenger[];
  flights: Flight[];
  payments: Payment[];
}

// ── Request / Response DTOs ───────────────────────────────────────────────────

export interface EdifactResponse {
  reservationId: number;
  recordLocator: string;
  edifactContent: string;
  generatedAt: string;
}

export interface ManifestRequest {
  passengerCount?: number;
  airline?: string;
  flightNumber?: string;
  receiver?: string;
}

export interface ManifestResponse {
  edifactContent: string;
  passengerCount: number;
  generatedAt: string;
}

export interface SampleDataRequest {
  passengerCount?: number;
  flightCount?: number;
  includeBags?: boolean;
  includeSeats?: boolean;
  includeDocuments?: boolean;
  includePayment?: boolean;
  includeCodeshare?: boolean;
  includeThruFlight?: boolean;
  includePhoneNumbers?: boolean;
  includeAgencyInfo?: boolean;
  includeCreditCard?: boolean;
  receiver?: string;
}

export interface SampleDataResponse {
  reservationId: number;
  recordLocator: string;
  passengerCount: number;
  flightCount: number;
  message: string;
}

export interface MultipleSampleDataRequest extends SampleDataRequest {
  count?: number;
}

export interface MultipleSampleDataResponse {
  totalGenerated: number;
  reservations: SampleDataResponse[];
  message: string;
}

export interface BulkGenerationRequest {
  fileCount?: number;
  minPassengers?: number;
  maxPassengers?: number;
  minFlights?: number;
  maxFlights?: number;
  receiver?: string;
}

export interface BulkGenerationResponse {
  files: GeneratedFile[];
  totalFiles: number;
  generatedAt: string;
}

// ── Service ───────────────────────────────────────────────────────────────────

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private base = environment.apiUrl;
  private p = environment.apiPaths;

  constructor(private http: HttpClient) {}

  // ── Edifact ────────────────────────────────────────────────────────────────

  generateEdifact(): Observable<EdifactResponse> {
    return this.http.post<EdifactResponse>(`${this.base}/${this.p.edifact}/generate`, {});
  }

  getEdifactById(reservationId: number): Observable<EdifactResponse> {
    return this.http.get<EdifactResponse>(`${this.base}/${this.p.edifact}/generate/${reservationId}`);
  }

  downloadEdifact(reservationId: number): Observable<Blob> {
    return this.http.get(`${this.base}/${this.p.edifact}/download/${reservationId}`, { responseType: 'blob' });
  }

  generateManifest(request?: ManifestRequest): Observable<ManifestResponse> {
    return this.http.post<ManifestResponse>(`${this.base}/${this.p.edifact}/manifest/generate`, request ?? {});
  }

  downloadManifest(request?: ManifestRequest): Observable<Blob> {
    return this.http.post(`${this.base}/${this.p.edifact}/manifest/download`, request ?? {}, { responseType: 'blob' });
  }

  // ── Bulk Edifact ───────────────────────────────────────────────────────────

  generateBulk(request: BulkGenerationRequest): Observable<BulkGenerationResponse> {
    return this.http.post<BulkGenerationResponse>(`${this.base}/${this.p.bulkEdifact}/generate`, request);
  }

  // ── Sample Data ────────────────────────────────────────────────────────────

  generateSampleData(request?: SampleDataRequest): Observable<SampleDataResponse> {
    return this.http.post<SampleDataResponse>(`${this.base}/${this.p.sampleData}/generate`, request ?? {});
  }

  generateMultipleSamples(request?: MultipleSampleDataRequest): Observable<MultipleSampleDataResponse> {
    return this.http.post<MultipleSampleDataResponse>(`${this.base}/${this.p.sampleData}/generate-multiple`, request ?? {});
  }

  // ── Reservations ───────────────────────────────────────────────────────────

  getReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.base}/${this.p.reservations}`);
  }

  getReservation(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.base}/${this.p.reservations}/${id}`);
  }

  getReservationByLocator(recordLocator: string): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.base}/${this.p.reservations}/by-locator/${recordLocator}`);
  }

  createReservation(reservation: Partial<Reservation>): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.base}/${this.p.reservations}`, reservation);
  }

  updateReservation(id: number, reservation: Partial<Reservation>): Observable<void> {
    return this.http.put<void>(`${this.base}/${this.p.reservations}/${id}`, reservation);
  }

  deleteReservation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${this.p.reservations}/${id}`);
  }

  deleteAllReservations(): Observable<void> {
    return this.http.delete<void>(`${this.base}/${this.p.reservations}/all`);
  }
}

