import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { DateUtils } from 'ng-jhipster';

import { Aspect } from './aspect.model';
import { ResponseWrapper, createRequestOption } from '../../shared';

@Injectable()
export class AspectService {

    private resourceUrl = 'api/aspects';
    private resourceSearchUrl = 'api/_search/aspects';

    constructor(private http: Http, private dateUtils: DateUtils) { }

    create(aspect: Aspect): Observable<Aspect> {
        const copy = this.convert(aspect);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            this.convertItemFromServer(jsonResponse);
            return jsonResponse;
        });
    }

    update(aspect: Aspect): Observable<Aspect> {
        const copy = this.convert(aspect);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            this.convertItemFromServer(jsonResponse);
            return jsonResponse;
        });
    }

    find(id: number): Observable<Aspect> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            const jsonResponse = res.json();
            this.convertItemFromServer(jsonResponse);
            return jsonResponse;
        });
    }

    query(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
            .map((res: Response) => this.convertResponse(res));
    }

    delete(id: number): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${id}`);
    }

    search(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options)
            .map((res: any) => this.convertResponse(res));
    }

    private convertResponse(res: Response): ResponseWrapper {
        const jsonResponse = res.json();
        for (let i = 0; i < jsonResponse.length; i++) {
            this.convertItemFromServer(jsonResponse[i]);
        }
        return new ResponseWrapper(res.headers, jsonResponse, res.status);
    }

    private convertItemFromServer(entity: any) {
        entity.createdAt = this.dateUtils
            .convertLocalDateFromServer(entity.createdAt);
        entity.updatedAt = this.dateUtils
            .convertLocalDateFromServer(entity.updatedAt);
    }

    private convert(aspect: Aspect): Aspect {
        const copy: Aspect = Object.assign({}, aspect);
        copy.createdAt = this.dateUtils
            .convertLocalDateToServer(aspect.createdAt);
        copy.updatedAt = this.dateUtils
            .convertLocalDateToServer(aspect.updatedAt);
        return copy;
    }
}
