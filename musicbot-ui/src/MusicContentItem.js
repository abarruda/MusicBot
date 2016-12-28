import React, { Component } from 'react';
import moment from 'moment';
import {Image} from 'react-bootstrap';

class MusicContentItem extends Component {

	render() {
		let set = this.props.data;
		var date = moment(set.originalDate * 1000).format('MMMM Do YYYY, h:mm a');

		return (
	        <div key={set.url}>
	          <a href={set.url}><Image src={set.metadata.imageUrl} responsive /></a>
	          <h4>{set.metadata.title}</h4>
	            Originally posted by {set.originalUser.firstName} on {date}
	            <br />
	            {set.references.length} reference(s).
	        </div>
        );
	}

}

export default MusicContentItem