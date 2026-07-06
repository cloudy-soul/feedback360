import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { DashboardService } from '../../../core/services/dashboard.service';
import { Dashboard } from '../../../core/models/dashboard.model';

@Component({ selector: 'app-dashboard', standalone: true, imports: [CommonModule, BaseChartDirective], templateUrl: './dashboard.component.html' })
export class DashboardComponent implements OnInit {
  data: Dashboard | null = null; loading = true;

  pieData: ChartData<'pie', number[], string> = { labels: ['Submitted','Pending'], datasets: [{ data: [], backgroundColor: ['#27795A','#B7770D'] }] };
  pieOptions: ChartOptions<'pie'> = { plugins: { legend: { position: 'bottom' } }, responsive: true };

  donutData: ChartData<'doughnut', number[], string> = { labels: [], datasets: [{ data: [], backgroundColor: ['#1A3C6E','#8BAF9F','#E67E22','#9B59B6','#E74C3C'] }] };
  donutOptions: ChartOptions<'doughnut'> = { plugins: { legend: { position: 'right' } }, responsive: true };

  lineData: ChartData<'line', number[], string> = { labels: [], datasets: [{ label:'Avg Rating', data: [], borderColor:'#8BAF9F', backgroundColor:'#8BAF9F33', tension:0.4, fill:true, pointBackgroundColor:'#1A3C6E' }] };
  lineOptions: ChartOptions<'line'> = { plugins:{ legend:{ display:false } }, scales:{ y:{ min:0, max:10 } }, responsive: true };

  barData: ChartData<'bar', number[], string> = { labels: [], datasets: [{ label:'Avg Rating', data: [], backgroundColor:'#1A3C6E', borderRadius:6 }] };
  barOptions: ChartOptions<'bar'> = { indexAxis:'y', plugins:{ legend:{ display:false } }, scales:{ x:{ min:0, max:10 } }, responsive: true };

  constructor(private svc: DashboardService) {}

  ngOnInit(): void {
    this.svc.get().subscribe({ next: d => { this.data = d; this.loading = false; this.buildCharts(d); }, error: () => this.loading = false });
  }

  private buildCharts(d: Dashboard): void {
    this.pieData = { labels:['Submitted','Pending'], datasets:[{ data:[d.totalSubmitted, d.pendingCount], backgroundColor:['#27795A','#B7770D'] }] };
    const dl = Object.keys(d.byDepartment), dv = Object.values(d.byDepartment) as number[];
    this.donutData = { labels: dl, datasets:[{ data: dv, backgroundColor:['#1A3C6E','#8BAF9F','#E67E22','#9B59B6','#E74C3C','#3498DB'] }] };
    this.lineData = { labels: d.ratingTrend.map(t=>t.period), datasets:[{ label:'Avg Rating', data: d.ratingTrend.map(t=>t.avgRating), borderColor:'#8BAF9F', backgroundColor:'#8BAF9F33', tension:0.4, fill:true, pointBackgroundColor:'#1A3C6E' }] };
    this.barData = { labels: d.topModules.map(m=>m.title), datasets:[{ label:'Avg Rating', data: d.topModules.map(m=>m.avgRating), backgroundColor:'#1A3C6E', borderRadius:6 }] };
  }
}
