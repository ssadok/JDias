import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { Observable } from 'rxjs/Rx';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { Participation } from './participation.model';
import { ParticipationPopupService } from './participation-popup.service';
import { ParticipationService } from './participation.service';
import { Person, PersonService } from '../person';
import { ResponseWrapper } from '../../shared';

@Component({
    selector: 'jhi-participation-dialog',
    templateUrl: './participation-dialog.component.html'
})
export class ParticipationDialogComponent implements OnInit {

    participation: Participation;
    isSaving: boolean;

    people: Person[];

    constructor(
        public activeModal: NgbActiveModal,
        private alertService: JhiAlertService,
        private participationService: ParticipationService,
        private personService: PersonService,
        private eventManager: JhiEventManager
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
        this.personService.query()
            .subscribe((res: ResponseWrapper) => { this.people = res.json; }, (res: ResponseWrapper) => this.onError(res.json));
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.participation.id !== undefined) {
            this.subscribeToSaveResponse(
                this.participationService.update(this.participation));
        } else {
            this.subscribeToSaveResponse(
                this.participationService.create(this.participation));
        }
    }

    private subscribeToSaveResponse(result: Observable<Participation>) {
        result.subscribe((res: Participation) =>
            this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
    }

    private onSaveSuccess(result: Participation) {
        this.eventManager.broadcast({ name: 'participationListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    trackPersonById(index: number, item: Person) {
        return item.id;
    }
}

@Component({
    selector: 'jhi-participation-popup',
    template: ''
})
export class ParticipationPopupComponent implements OnInit, OnDestroy {

    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private participationPopupService: ParticipationPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.participationPopupService
                    .open(ParticipationDialogComponent as Component, params['id']);
            } else {
                this.participationPopupService
                    .open(ParticipationDialogComponent as Component);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
