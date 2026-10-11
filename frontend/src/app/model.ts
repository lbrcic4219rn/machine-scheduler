export interface LoginRequest {
  email: string;
  password: string;
}

export interface Permissions {
  canReadUser: 0 | 1;
  canCreateUser: 0 | 1;
  canUpdateUser: 0 | 1;
  canDeleteUser: 0 | 1;
  canSearchMachine: 0 | 1;
  canStartMachine: 0 | 1;
  canStopMachine: 0 | 1;
  canRestartMachine: 0 | 1;
  canCreateMachine: 0 | 1;
  canDestroyMachine: 0 | 1;
}

export type PermissionKey = keyof Permissions;

export interface User {
  id: number;
  name: string;
  surname: string;
  email: string;
  permission: Permissions;
}

export interface UserPayload {
  name: string;
  surname: string;
  email: string;
  password: string;
  permission: Permissions;
}

export type MachineStatus = 'RUNNING' | 'STOPPED';

export interface Machine {
  id: number;
  name: string;
  status: MachineStatus;
  createdAt: string;
  operationActive: boolean;
}

export interface ErrorMessage {
  errorDate: string;
  machineId: number;
  operationName: string;
  message: string;
}

export interface CsrfToken {
  headerName: string;
  token: string;
}

export interface Session {
  id: number;
  name: string;
  surname: string;
  email: string;
  permission: Permissions;
}

export interface MachineSearchFilters {
  name?: string;
  statusRunning: boolean;
  statusStopped: boolean;
  dateFrom?: string | null;
  dateTo?: string | null;
}
