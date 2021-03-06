import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { Reshare } from './reshare.model';
import { ResponseWrapper, createRequestOption } from '../../shared';
import {Post} from '../post/post.model';

@Injectable()
export class ReshareService {

    private resourceUrl = 'api/reshares';
    private resourceSearchUrl = 'api/_search/reshares';

    constructor(private http: Http) { }

    create(reshare: Reshare): Observable<Reshare> {
        const copy = this.convert(reshare);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    createReshare(copy) {
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    update(reshare: Reshare): Observable<Reshare> {
        const copy = this.convert(reshare);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    find(id: number): Observable<Reshare> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            return res.json();
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
        return new ResponseWrapper(res.headers, jsonResponse, res.status);
    }

    private convert(reshare: Reshare): Reshare {
        const copy: Reshare = Object.assign({}, reshare);
        return copy;
    }
}
