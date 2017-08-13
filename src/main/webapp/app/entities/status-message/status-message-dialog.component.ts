import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { Observable } from 'rxjs/Rx';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { StatusMessage } from './status-message.model';
import { StatusMessagePopupService } from './status-message-popup.service';
import { StatusMessageService } from './status-message.service';
import { Location, LocationService } from '../location';
import { Poll, PollService } from '../poll';
import { ResponseWrapper } from '../../shared';

@Component({
    selector: 'jhi-status-message-dialog',
    templateUrl: './status-message-dialog.component.html'
})
export class StatusMessageDialogComponent implements OnInit {

    statusMessage: StatusMessage;
    isSaving: boolean;

    locations: Location[];

    polls: Poll[];

    constructor(
        public activeModal: NgbActiveModal,
        private alertService: JhiAlertService,
        private statusMessageService: StatusMessageService,
        private locationService: LocationService,
        private pollService: PollService,
        private eventManager: JhiEventManager
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
        this.locationService
            .query({filter: 'statusmessage-is-null'})
            .subscribe((res: ResponseWrapper) => {
                if (!this.statusMessage.location || !this.statusMessage.location.id) {
                    this.locations = res.json;
                } else {
                    this.locationService
                        .find(this.statusMessage.location.id)
                        .subscribe((subRes: Location) => {
                            this.locations = [subRes].concat(res.json);
                        }, (subRes: ResponseWrapper) => this.onError(subRes.json));
                }
            }, (res: ResponseWrapper) => this.onError(res.json));
        this.pollService
            .query({filter: 'statusmessage-is-null'})
            .subscribe((res: ResponseWrapper) => {
                if (!this.statusMessage.poll || !this.statusMessage.poll.id) {
                    this.polls = res.json;
                } else {
                    this.pollService
                        .find(this.statusMessage.poll.id)
                        .subscribe((subRes: Poll) => {
                            this.polls = [subRes].concat(res.json);
                        }, (subRes: ResponseWrapper) => this.onError(subRes.json));
                }
            }, (res: ResponseWrapper) => this.onError(res.json));
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.statusMessage.id !== undefined) {
            this.subscribeToSaveResponse(
                this.statusMessageService.update(this.statusMessage));
        } else {
            this.subscribeToSaveResponse(
                this.statusMessageService.create(this.statusMessage));
        }
    }

    private subscribeToSaveResponse(result: Observable<StatusMessage>) {
        result.subscribe((res: StatusMessage) =>
            this.onSaveSuccess(res), (res: Response) => this.onSaveError(res));
    }

    private onSaveSuccess(result: StatusMessage) {
        this.eventManager.broadcast({ name: 'statusMessageListModification', content: 'OK'});
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

    trackLocationById(index: number, item: Location) {
        return item.id;
    }

    trackPollById(index: number, item: Poll) {
        return item.id;
    }
}

@Component({
    selector: 'jhi-status-message-popup',
    template: ''
})
export class StatusMessagePopupComponent implements OnInit, OnDestroy {

    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private statusMessagePopupService: StatusMessagePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.statusMessagePopupService
                    .open(StatusMessageDialogComponent as Component, params['id']);
            } else {
                this.statusMessagePopupService
                    .open(StatusMessageDialogComponent as Component);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
